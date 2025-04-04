package com.soda.project;

import com.soda.article.entity.Article;
import com.soda.common.BaseEntity;
import com.soda.project.task.Task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<Task> taskList = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Stage(String name, Float stageOrder, Project project) {
        this.name = name;
        this.stageOrder = stageOrder;
        this.project = project;
    }

    public static List<Stage> create(Project project) {
        final float INITIAL_ORDER = 1.0f;
        final List<String> INITIAL_STAGE_NAMES = Arrays.asList("요구사항 정의", "화면 설계", "디자인", "퍼블리싱", "개발", "검수");
        final float ORDER_INCREMENT = 1.0f;

        List<Stage> stages = new ArrayList<>();
        float order = INITIAL_ORDER;

        for (String name : INITIAL_STAGE_NAMES) {
            Stage stage = Stage.builder()
                    .project(project)
                    .name(name)
                    .stageOrder(order)
                    .build();
            stages.add(stage);
            order += ORDER_INCREMENT;
        }
        return stages;
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void moveStageOrder(Float stageOrder) {
        this.stageOrder = stageOrder;
    }
}
