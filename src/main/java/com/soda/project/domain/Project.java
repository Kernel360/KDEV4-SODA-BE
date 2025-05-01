package com.soda.project.domain;

import com.soda.common.BaseEntity;
import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.enums.ProjectStatus;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.stage.request.error.RequestErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Collection<Member> members;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<MemberProject> memberProjects = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CompanyProject> companyProjects = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Stage> stage = new ArrayList<>();

    public Project() {
        var reqeusts = stage.stream()
                .flatMap(it -> it.getRequestList().stream())
                .toList();
        var members = memberProjects.stream()
                .map(it -> new Member(it.getMember().getId(),
                        reqeusts.stream().filter(r -> r.getMember().getId().equals(it.getMember().getId())).toList()))
                .toList();

        this.members = members;
    }

    @Builder
    public Project(String title, String description, LocalDateTime startDate, LocalDateTime endDate, ProjectStatus status) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void delete() {
        this.markAsDeleted();
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

    public void approveRequest(Long memberId, Long requestId) {
        var member = members.stream().filter(it -> it.id().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_CLI));

        var request = member.requests().stream().filter(it -> it.getId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new GeneralException(RequestErrorCode.USER_IS_NOT_APPROVER));

        request.approved();
    }
}
