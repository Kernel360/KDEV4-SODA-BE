package com.soda.project.entity;

import com.soda.article.entity.Article;
import com.soda.common.BaseEntity;
import com.soda.request.entity.Request;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Float stageOrder;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<Article> articleList = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<Request> requestList = new ArrayList<>();

    @Builder
    public Stage(String name, Float stageOrder, Project project) {
        this.name = name;
        this.stageOrder = stageOrder;
        this.project = project;
    }

    public void updateName(String newName) {
        this.name = newName;
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void moveStageOrder(Float stageOrder) {
        this.stageOrder = stageOrder;
    }
}
