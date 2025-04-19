package com.soda.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DevCompanyAssignmentRequest {

    @NotNull
    @Valid
    private List<CompanyAssignment> devAssignments;

}
