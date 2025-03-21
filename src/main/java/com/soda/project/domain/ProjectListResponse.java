package com.soda.project.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListResponse {

    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String devCompanyName;
    private String clientCompanyName;

}
