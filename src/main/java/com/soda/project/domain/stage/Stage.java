package com.soda.project.domain.stage;

import com.soda.project.domain.stage.article.Article;
import com.soda.common.BaseEntity;
import com.soda.project.domain.Project;
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
    private static final float INITIAL_ORDER = 1000.0f;
    private static final float ORDER_INCREMENT = 1000.0f;
    private static final List<String> DEFAULT_INITIAL_STAGE_NAMES = List.of(
            "요구사항 정의", "화면 설계", "디자인", "퍼블리싱", "개발", "검수"
    );

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

    public static List<Stage> createInitialStages(Project project, List<String> initialStageNames) {
        List<String> namesToUse = (CollectionUtils.isEmpty(initialStageNames))
                ? DEFAULT_INITIAL_STAGE_NAMES
                : initialStageNames;

        if (namesToUse.isEmpty()) {
            return Collections.emptyList(); // 생성할 이름 없으면 빈 리스트 반환
        }
        List<Stage> initialStages = new ArrayList<>();
        float currentOrder = INITIAL_ORDER;

        for (String name : namesToUse) {
            // 각 스테이지 생성 (Builder 사용)
            Stage stage = Stage.builder()
                    .project(project) // 연관 프로젝트 설정
                    .name(name.trim()) // 이름 설정 (공백 제거)
                    .stageOrder(currentOrder) // 순서 설정
                    .build();
            initialStages.add(stage);
            currentOrder += ORDER_INCREMENT; // 다음 순서 계산
        }

        return initialStages;
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
