package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.dto.*;
import com.soda.project.enums.ProjectStatus;
import com.soda.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponseForm<Page<ProjectListResponse>>> getAllProjects(@RequestParam(required = false) ProjectStatus status,
                                                                                     @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectListResponse> projectList = projectService.getAllProjects(status, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(projectList));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponseForm<Page<ProjectListResponse>>> getMyProjects(HttpServletRequest request,
                                                                                    @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = (Long) request.getAttribute("memberId");
        String userRole = (String) request.getAttribute("userRole").toString();
        Page<ProjectListResponse> response = projectService.getMyProjects(userId, userRole, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }
}
