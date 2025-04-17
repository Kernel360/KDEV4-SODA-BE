package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.dto.*;
import com.soda.project.enums.ProjectStatus;
import com.soda.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> createProject(HttpServletRequest request,
                                                                                @Valid @RequestBody ProjectCreateRequest projectCreateRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectCreateResponse newProject = projectService.createProject(userRole, projectCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(newProject, "project 생성 성공"));
    }

    @PostMapping("/{projectId}/dev-companies")
    public ResponseEntity<ApiResponseForm<DevCompanyAssignmentResponse>> assignDevCompany(@PathVariable Long projectId,
                                                                                          HttpServletRequest request,
                                                                                          @Valid @RequestBody DevCompanyAssignmentRequest devCompanyAssignmentRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        DevCompanyAssignmentResponse response = projectService.assignDevCompany(projectId, userRole, devCompanyAssignmentRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "project에 개발사 지정 성공"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponseForm<List<ProjectListResponse>>> getAllProjects(@RequestParam(required = false) ProjectStatus status,
                                                                                     Pageable pageable) {
        List<ProjectListResponse> projectList = projectService.getAllProjects(status, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(projectList));
    }
}
