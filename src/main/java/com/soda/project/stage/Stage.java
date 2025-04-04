package com.soda.project.stage;

import com.soda.article.entity.Article;
import com.soda.common.BaseEntity;
import com.soda.global.response.GeneralException;
import com.soda.project.Project;
import com.soda.project.stage.task.Task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stage extends BaseEntity {
    private static final float INITIAL_ORDER = 1.0f;
    private static final List<String> INITIAL_STAGE_NAMES = Arrays.asList("요구사항 정의", "화면 설계", "디자인", "퍼블리싱", "개발", "검수");
    private static final float ORDER_INCREMENT = 1.0f;

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

    public static Stage create(String name, Project project, Stage prevStage, Stage nextStage) {
        return Stage.builder()
             .stageOrder(calculateNewOrder(project, prevStage, nextStage))
             .name(name)
             .project(project)
             .build();
    }

    public void move(Stage prevStage, Stage nextStage) {
        float newOrder = calculateNewOrder(this.project, prevStage, nextStage);
        moveStageOrder(newOrder);
    }

    /**
     *  이전/다음 단계 ID를 기반으로 새 단계의 순서(`stageOrder`)를 계산합니다.
     * @param project     단계가 속할 프로젝트
     * @param prevStageId 이전 단계 ID (없으면 null)
     * @param nextStageId 다음 단계 ID (없으면 null)
     * @return 계산된 새 단계의 순서값 (float)
     * @throws GeneralException 참조된 단계 ID가 존재하지 않거나(`STAGE_NOT_FOUND`),
     *                          해당 프로젝트 소속이 아니거나(`STAGE_PROJECT_MISMATCH`),
     *                          순서 설정이 유효하지 않은 경우(`INVALID_STAGE_ORDER`) 발생
     */
    private static float calculateNewOrder(Project project, Stage prevStage, Stage nextStage) {
        Float prevOrder = null;
        Float nextOrder = null;

        if (prevStage != null) {
            if (!prevStage.getProject().getId().equals(project.getId())) {
                log.error("단계 순서 계산 실패: 이전 단계 ID {} 가 프로젝트 ID {} 에 속하지 않음", prevStage.getId(), project.getId());
                throw new GeneralException(StageErrorCode.STAGE_PROJECT_MISMATCH);
            }
            prevOrder = prevStage.getStageOrder();
        }

        if (nextStage != null) {
            if (!nextStage.getProject().getId().equals(project.getId())) {
                log.error("단계 순서 계산 실패: 다음 단계 ID {} 가 프로젝트 ID {} 에 속하지 않음", nextStage.getId(), project.getId());
                throw new GeneralException(StageErrorCode.STAGE_PROJECT_MISMATCH);
            }
            nextOrder = nextStage.getStageOrder();
        }

        if (prevOrder != null && nextOrder != null) {
            if (prevOrder >= nextOrder) {
                log.error("단계 순서 계산 실패: 이전 단계 순서({}) >= 다음 단계 순서({})", prevOrder, nextOrder);
                throw new GeneralException(StageErrorCode.INVALID_STAGE_ORDER);
            }
            return (prevOrder + nextOrder) / 2.0f;
        } else if (prevOrder != null) {
            return prevOrder + ORDER_INCREMENT;
        } else if (nextOrder != null) {
            if (nextOrder <= 0) {
                log.warn("다음 단계 순서({})가 0 이하입니다. 순서를 {}로 설정합니다.", nextOrder, nextOrder / 2.0f);
            }
            return nextOrder / 2.0f;
        } else {
            log.info("프로젝트 ID {} 에 첫 단계 추가 또는 이동. 초기 순서 {} 적용", project.getId(), INITIAL_ORDER);
            return INITIAL_ORDER;
        }
    }

    public void delete() {
        this.markAsDeleted();
    }

    public void moveStageOrder(Float stageOrder) {
        this.stageOrder = stageOrder;
    }
}
