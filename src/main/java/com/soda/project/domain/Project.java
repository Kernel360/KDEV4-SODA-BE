package com.soda.project.domain;

import com.soda.common.BaseEntity;
import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.member.MemberProject;
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
}
