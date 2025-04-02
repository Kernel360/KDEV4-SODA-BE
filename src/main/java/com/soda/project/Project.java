package com.soda.project;

import com.soda.common.BaseEntity;
import com.soda.member.Company;
import com.soda.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
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

    protected void updateProject(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
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
}
