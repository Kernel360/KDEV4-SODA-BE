package com.soda.project.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCreateRequest {

    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long clientCompanyId;           // 고객사 ID
    private Long devCompanyId;              // 개발사 ID
    private List<Long> clientMembers;       // 고객사 멤버 ID
    private List<Long> devMembers;          // 개발사 멤버 ID

}
