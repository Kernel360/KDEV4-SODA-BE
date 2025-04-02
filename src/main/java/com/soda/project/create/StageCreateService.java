package com.soda.project.create;

import com.soda.global.response.GeneralException;
import com.soda.project.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StageCreateService {

    private static final float INITIAL_ORDER = 1.0f;
    private static final List<String> INITIAL_STAGE_NAMES = Arrays.asList(
            "요구사항 정의", "화면 설계", "디자인", "퍼블리싱", "개발", "검수"
    );
    private static final float ORDER_INCREMENT = 1.0f;

    private final StageRepository stageRepository;
    private final ProjectRepository projectRepository;

    /**
     * 특정 프로젝트에 미리 정의된 초기 단계들을 일괄 생성합니다.
     * (예: "요구사항 정의", "화면 설계" 등)
     *
     * @param projectId 초기 단계를 생성할 프로젝트의 ID
     * @return 생성된 초기 단계들의 DTO 목록 (`List<StageReadResponse>`)
     * @throws GeneralException 프로젝트(`ProjectErrorCode.PROJECT_NOT_FOUND`)를 찾을 수 없는 경우 발생합니다.
     */
    @Transactional
    public List<StageReadResponse> createInitialStages(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        List<Stage> initialStages = createStagesInternal(project);
        stageRepository.saveAll(initialStages);


        return initialStages.stream()
                .map(StageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }


    /**
     *  주어진 프로젝트에 대해 초기 단계 엔티티 리스트를 생성합니다.
     * @param project 단계를 생성할 프로젝트 엔티티
     * @return 생성된 Stage 엔티티 리스트 (DB 저장 전 상태)
     */
    private List<Stage> createStagesInternal(Project project) {
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
}
