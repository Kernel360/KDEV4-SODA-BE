package com.soda.project;

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
public class ProjectRequest {

    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long clientCompanyId;           // 고객사 ID
    private Long devCompanyId;              // 개발사 ID
    private List<Long> devManagers;         // 개발사 담당자들 ID
    private List<Long> devMembers;          // 개발사 멤버들 ID
    private List<Long> clientManagers;      // 고객사 담당자들 ID
    private List<Long> clientMembers;       // 고객사 멤버들 ID

}
