package com.soda.project;

import com.soda.common.BaseEntity;
import com.soda.global.response.GeneralException;
import com.soda.member.Company;
import com.soda.member.Member;
import com.soda.project.company.CompanyProject;
import com.soda.project.member.MemberProject;
import com.soda.project.stage.Stage;
import com.soda.project.stage.StageCreateRequest;
import com.soda.project.stage.StageErrorCode;
import com.soda.project.stage.StageResponse;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CompanyProject> companyProjects = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Stage> stage = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Project(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    protected static Project create(ProjectRequest request, Company devCompany, Company clientCompany, List<Member> devManagers,
                                    List<Member> devMembers, List<Member> clientManagers, List<Member> clientMembers) {
        var project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        project.assignCompanies(devCompany, clientCompany);
        project.assignMembers(devManagers, devMembers, clientManagers, clientMembers);
        project.addInitialStages(Stage.create(project));

        return project;
    }

    protected void addInitialStages(List<Stage> stages) {
        this.stage.addAll(stages);
    }

    protected void assignMembers(List<Member> devManagers, List<Member> devMembers, List<Member> clientManagers, List<Member> clientMembers) {
        var devMemberProject = devMembers.stream()
                .map(member -> MemberProject.createDevMember(member, this))
                .toList();
        var devManagerProject = devManagers.stream()
                .map(member -> MemberProject.createDevManager(member, this))
                .toList();
        var clientMemberProject = clientMembers.stream()
                .map(member -> MemberProject.createClientMember(member, this))
                .toList();
        var clientManagerProject = clientManagers.stream()
                .map(member -> MemberProject.createClientManager(member, this))
                .toList();

        this.memberProjects.addAll(devMemberProject);
        this.memberProjects.addAll(devManagerProject);
        this.memberProjects.addAll(clientMemberProject);
        this.memberProjects.addAll(clientManagerProject);
    }

    protected void assignCompanies(Company devCompany, Company clientCompany) {
        var devCompanyProject = CompanyProject.createDevCompany(devCompany, this);
        var clientCompanyProject = CompanyProject.createClientCompany(clientCompany, this);

        this.companyProjects.add(devCompanyProject);
        this.companyProjects.add(clientCompanyProject);
    }

    protected void delete() {
        this.markAsDeleted();
        memberProjects.forEach(MemberProject::delete);
        companyProjects.forEach(CompanyProject::delete);
        stage.forEach(Stage::delete);
    }

    protected void updateProjectInfo(String title, String description, LocalDateTime startDate, LocalDateTime endDate,
                                     Company devCompany, Company clientCompany, List<Member> devManagers, List<Member> devMembers, List<Member> clientManagers, List<Member> clientMembers) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;

        assignCompanies(devCompany, clientCompany);
        assignMembers(devManagers, devMembers, clientManagers, clientMembers);
    }

    protected ProjectResponse toResponse() {
        var companyName = companyProjects.stream().map(cp -> cp.getCompany().getName()).findAny().orElseThrow();

        return ProjectResponse.builder()
                .projectId(this.getId())
                .title(this.getTitle())
                .description(this.getDescription())
                .startDate(this.getStartDate())
                .endDate(this.getEndDate())
                .devCompanyName(companyName)
                // todo: 이건 귀찮아서 걍 넘김 이게 중요한게 아닌걸로 생각함
//                .devCompanyManagers(extractMemberNames(this.getMemberProjects()))
//                .devCompanyMembers(extractMemberNames(devParticipants))
//                .clientCompanyName(clientManagers.get(0).getCompany().getName())
//                .clientCompanyManagers(extractMemberNames(clientManagers))
//                .clientCompanyMembers(extractMemberNames(clientParticipants))
                .build();
    }

    /**
     * 새로운 단계를 프로젝트에 추가합니다.
     * 단계 순서는 요청에 포함된 이전/다음 단계 ID(`prevStageId`, `nextStageId`)를 기반으로 서버에서 계산됩니다.
     * 프로젝트의 활성 단계는 최대 10개까지 가능합니다.
     *
     * @param request 단계 생성 요청 정보 (projectId, name, prevStageId, nextStageId 포함)
     * @return 생성된 단계 정보 DTO (`StageResponse`)
     * @throws GeneralException 프로젝트(`ProjectErrorCode.PROJECT_NOT_FOUND`) 또는 참조된 이전/다음 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없거나,
     *                          단계 개수 제한(`StageErrorCode.STAGE_LIMIT_EXCEEDED`)을 초과했거나,
     *                          참조된 단계가 다른 프로젝트 소속(`StageErrorCode.STAGE_PROJECT_MISMATCH`)이거나,
     *                          순서 설정이 유효하지 않은(`StageErrorCode.INVALID_STAGE_ORDER`) 경우 발생합니다.
     */
    public StageResponse addStage(StageCreateRequest request) {
        int activeStageCount = this.stage.stream().filter(it -> !it.getIsDeleted()).toList().size();

        if (activeStageCount >= 10) {
            log.warn("단계 추가 실패: 프로젝트 ID {} 의 활성 단계 개수 제한(10개) 초과 (현재 {}개)", this.getId(), activeStageCount);
            throw new GeneralException(StageErrorCode.STAGE_LIMIT_EXCEEDED);
        }

        var prevStage = this.stage.stream().filter(it -> it.getId().equals(request.getPrevStageId())).findFirst();
        var nextStage = this.stage.stream().filter(it -> it.getId().equals(request.getNextStageId())).findFirst();

        Stage stage = Stage.create(request.getName(), this, prevStage.orElse(null), nextStage.orElse(null));

        log.info("단계 추가 성공: 프로젝트 ID {}, 새 단계 ID {}, 순서 {}",
                this.getId(), stage.getId(), stage.getStageOrder());
        return StageResponse.fromEntity(stage);
    }

    /**
     * 특정 단계를 논리적으로 삭제합니다.
     * 실제 DB 레코드를 삭제하는 대신, `isDeleted` 와 같은 플래그를 true로 설정합니다.
     *
     * @param stageId 논리적으로 삭제할 단계의 ID
     * @throws GeneralException 삭제할 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없는 경우 발생합니다.
     */
    public void deleteStage(Long stageId) {
        Stage stage = this.stage.stream().filter(it -> it.getId().equals(stageId)).findFirst().orElseThrow();

        stage.delete();

        log.info("단계 삭제 성공 (논리적): 단계 ID {}", stageId);
    }

    /**
     * 특정 단계의 순서(위치)를 변경합니다.
     * 새로운 위치는 요청의 `prevStageId`와 `nextStageId`에 의해 정의되며, 실제 순서 값은 서버에서 계산됩니다.
     *
     * @param stageId 순서를 변경할 단계의 ID
     * @param request 새로운 위치를 정의하는 요청 정보 (prevStageId, nextStageId 포함)
     * @throws GeneralException 이동할 단계(`StageErrorCode.STAGE_NOT_FOUND`) 또는 참조된 이전/다음 단계(`StageErrorCode.STAGE_NOT_FOUND`)를 찾을 수 없거나,
     *                          참조된 단계가 다른 프로젝트 소속(`StageErrorCode.STAGE_PROJECT_MISMATCH`)이거나,
     *                          순서 설정이 유효하지 않은(`StageErrorCode.INVALID_STAGE_ORDER`) 경우 발생합니다.
     *                          (이동할 단계에 프로젝트 정보가 없는 비정상 상태 포함)
     */
    public void moveStage(Long stageId, Long prevStageId, Long nextStageId) {
        var stage = this.stage.stream().filter(it -> it.getId().equals(stageId)).findFirst().orElseThrow();
        var prevStage = this.stage.stream().filter(it -> it.getId().equals(prevStageId)).findFirst().orElseThrow();
        var nextStage = this.stage.stream().filter(it -> it.getId().equals(nextStageId)).findFirst().orElseThrow();


        stage.move(prevStage, nextStage);
    }

    public List<Stage> getActiveStages() {
        return this.stage.stream().filter(it -> !it.getIsDeleted()).toList();
    }
}
