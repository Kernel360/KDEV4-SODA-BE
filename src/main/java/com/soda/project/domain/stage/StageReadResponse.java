package com.soda.project.domain.stage;

import com.soda.project.domain.task.TaskReadResponse;
import com.soda.project.entity.Stage;
import com.soda.project.entity.Task;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StageReadResponse {
    private Long id;
    private String name;
    private Float stageOrder;
    private List<TaskReadResponse> tasks;

    private StageReadResponse() {}

    /**
     * Stage 엔티티로부터 StageReadResponse DTO를 생성합니다.
     * Task 목록은 isDeleted가 false인 Task만 포함하며,
     * 포함된 Task들은 taskOrder 필드의 오름차순(낮은 값에서 높은 값)으로 정렬됩니다.
     *
     * @param stage 변환할 Stage 엔티티
     * @return 변환된 StageReadResponse DTO
     */
    public static StageReadResponse fromEntity(Stage stage) {
        StageReadResponse dto = new StageReadResponse();
        dto.id = stage.getId();
        dto.name = stage.getName();
        dto.stageOrder = stage.getStageOrder();

        if (stage.getTaskList() != null) {
            dto.tasks = stage.getTaskList().stream()
                    .filter(task -> Boolean.FALSE.equals(task.getIsDeleted()))
                    .sorted(Comparator.comparing(Task::getTaskOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(TaskReadResponse::fromEntity)
                    .collect(Collectors.toList());
        } else {
            dto.tasks = List.of();
        }

        return dto;
    }
}
