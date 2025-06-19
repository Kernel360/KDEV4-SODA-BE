package com.soda.project.domain.stage;

import com.soda.common.BaseEntity;
import com.soda.project.domain.Project;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.request.Request;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
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
        this.name = name.trim();
        this.stageOrder = stageOrder;
        this.project = project;
    }

    public static Stage createStage(Project project, String name,float newOrder) {
        return Stage.builder()
                .project(project)
                .name(name)
                .stageOrder(newOrder)
                .build();
    }

    public static List<Stage> createInitialStages(Project project, List<String> initialStageNames) {
        if (CollectionUtils.isEmpty(initialStageNames)) {
            return Collections.emptyList();
        }

        List<Stage> initialStages = new ArrayList<>();
        float currentOrder = StageConstants.INITIAL_ORDER;
        for (String name : initialStageNames) {
            Stage stage = Stage.builder()
                    .project(project)
                    .name(name)
                    .stageOrder(currentOrder)
                    .build();
            initialStages.add(stage);
            currentOrder += StageConstants.ORDER_INCREMENT;
}
        return initialStages;
    }


    public void updateName(String newName) {
        if (!this.name.equals(newName)) {
            this.name = newName.trim();
        }
    }

    public void delete() {
        this.markAsDeleted();
    }

    public boolean moveStageOrder(Float newStageOrder) {
        if (Float.compare(this.stageOrder, newStageOrder) != 0) {
            this.stageOrder = newStageOrder;
            return true;
        }
        return false;
    }

    public static float calculateNewOrder(Float prevOrder, Float nextOrder) {
        if (prevOrder == null && nextOrder == null) {
            return StageConstants.INITIAL_ORDER;
        } else if (prevOrder == null) {
            return nextOrder - StageConstants.ORDER_INCREMENT;
        } else if (nextOrder == null) {
            return prevOrder + StageConstants.ORDER_INCREMENT;
        } else {
            return (prevOrder + nextOrder) / 2.0f;
        }
    }
}
