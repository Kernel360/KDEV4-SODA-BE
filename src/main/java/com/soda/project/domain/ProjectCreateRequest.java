package com.soda.project.domain;

import com.soda.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private String description;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long clientCompanyId;           // 고객사 ID
    private Long devCompanyId;              // 개발사 ID
    private List<Long> devManagers;         // 개발사 담당자들 ID
    private List<Long> devMembers;          // 개발사 멤버들 ID
    private List<Long> clientManagers;      // 고객사 담당자들 ID
    private List<Long> clientMembers;       // 고객사 멤버들 ID

}
