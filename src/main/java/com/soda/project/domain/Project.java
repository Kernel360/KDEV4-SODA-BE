package com.soda.project.domain;

import com.soda.common.BaseEntity;
import com.soda.member.domain.member.Member;
import com.soda.member.domain.company.Company;
import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.member.MemberProject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

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

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CompanyProject> companyProjects = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Stage> stage = new ArrayList<>();

    @Builder
    public Project(String title, String description, LocalDateTime startDate, LocalDateTime endDate,ProjectStatus status) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    protected static Project create(String title, String description, LocalDateTime startDate, LocalDateTime endDate,
                                    List<Company> clientCompanies, List<Member> clientManagers, List<Member> clientMembers,
                                    List<String> initialStageNames) {
        var project = Project.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .status(ProjectStatus.CONTRACT)
                .build();

        project.assignClientCompanies(clientCompanies);
        project.assignClientMembers(clientManagers, clientMembers);
        project.addInitialStages(Stage.createInitialStages(project, initialStageNames));
        return project;
    }

    private void addInitialStages(List<Stage> initialStages) {
        if (initialStages != null) {
            this.stage.addAll(initialStages);
        }
    }

    protected void assignClientMembers(List<Member> clientManagers, List<Member> clientMembers) {
        if (clientManagers != null) {
        List<MemberProject> clientManagerProjects = clientManagers.stream()
                .map(member -> MemberProject.createClientManager(member, this))
                .toList();
        this.memberProjects.addAll(clientManagerProjects);
    }
        if (clientMembers != null) {
            List<MemberProject> clientMemberProjects = clientMembers.stream()
                    .map(member -> MemberProject.createClientMember(member, this))
                    .toList();
            this.memberProjects.addAll(clientMemberProjects);
        }
    }

    protected void assignClientCompanies(List<Company> clientCompanies) {
        if (!CollectionUtils.isEmpty(clientCompanies)) {
            List<CompanyProject> clientCompanyProjects = clientCompanies.stream()
                    .distinct()
                    .map(company -> CompanyProject.createClientCompany(company, this))
                    .toList();
            this.companyProjects.addAll(clientCompanyProjects);
        }
    }

    protected void assignDevMembers(List<Member> devManagers, List<Member> devMembers) {
        if (devManagers != null) {
            List<MemberProject> devManagerProjects = devManagers.stream()
                    .map(member -> MemberProject.createDevManager(member, this))
                    .toList();
            this.memberProjects.addAll(devManagerProjects);
        }
        if (devMembers != null) {
            List<MemberProject> devMemberProjects = devMembers.stream()
                    .map(member -> MemberProject.createDevMember(member, this))
                    .toList();
            this.memberProjects.addAll(devMemberProjects);
        }
    }

    protected void assignDevCompanies(List<Company> devCompanies) {
        if (!CollectionUtils.isEmpty(devCompanies)) {
            List<CompanyProject> devCompanyProjects = devCompanies.stream()
                    .distinct()
                    .map(company -> CompanyProject.createDevCompany(company, this))
                    .toList();
            this.companyProjects.addAll(devCompanyProjects);
        }
    }

    public void delete() {
        this.markAsDeleted();
        this.companyProjects.forEach(CompanyProject::delete);
        this.memberProjects.forEach(MemberProject::delete);
    }

    public void updateProjectInfo(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void changeStatus(ProjectStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }
}
