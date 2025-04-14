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

    @NotBlank(message = "프로젝트 제목은 필수입니다.")
    @Size(max = 255, message = "프로젝트 제목은 255자를 넘을 수 없습니다.")
    private String title;

    @Size(max = 5000, message = "프로젝트 설명은 5000자를 넘을 수 없습니다.")
    private String description;

    private ProjectStatus status;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;

    @NotNull(message = "고객사 ID는 필수입니다.")
    private Long clientCompanyId;

    @NotNull(message = "개발사 ID는 필수입니다.")
    private Long devCompanyId;

    @NotEmpty(message = "개발사 담당자는 최소 1명 이상 지정해야 합니다.")
    private List<Long> devManagers;

    private List<Long> devMembers;

    @NotEmpty(message = "고객사 담당자는 최소 1명 이상 지정해야 합니다.")
    private List<Long> clientManagers;

    private List<Long> clientMembers;

}
