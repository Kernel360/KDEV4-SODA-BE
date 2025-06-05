package com.soda.project.interfaces.stage.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GetMemberRequestCondition {
    private Long projectId;
    private String keyword;
    private LocalDateTime cursor;
}
