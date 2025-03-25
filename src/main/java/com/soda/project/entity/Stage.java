package com.soda.project.entity;

import com.soda.article.entity.Article;
import com.soda.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stage extends BaseEntity {

    private String name;

    private Long nextStageId;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<Article> articleList = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<Task> taskList = new ArrayList<>();

    @Builder
    public Stage(String name, Long nextStageId, String description, Project project) {
        this.name = name;
        this.nextStageId = nextStageId;
        this.description = description;
        this.project = project;
    }
}
