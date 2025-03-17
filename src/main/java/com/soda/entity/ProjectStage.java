package com.soda.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class ProjectStage extends BaseEntity{

    private String name;

    private Integer order;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "projectStage", cascade = CascadeType.ALL)
    private List<Article> articleList = new ArrayList<>();

    @OneToMany(mappedBy = "projectStage", cascade = CascadeType.ALL)
    private List<StageTask> stageTaskList = new ArrayList<>();
}
