package com.soda.request.dto;

import com.soda.request.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetRequestCondition {
    private Long stageId;
    private RequestStatus status;
}
