package com.soda.project.interfaces.stage.request.dto;

import com.soda.project.domain.stage.request.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetRequestCondition {
    private Long stageId;
    private RequestStatus status;
    private String keyword;
}
