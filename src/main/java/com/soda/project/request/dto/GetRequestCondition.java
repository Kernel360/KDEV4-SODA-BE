package com.soda.project.request.dto;

import com.soda.project.request.enums.RequestStatus;
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
