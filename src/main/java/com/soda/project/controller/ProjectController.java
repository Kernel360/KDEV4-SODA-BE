package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.*;
import com.soda.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectCreateResponse response = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 생성 성공"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponseForm<List<ProjectListResponse>>> getAllProjects() {
        List<ProjectListResponse> projectList = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponseForm.success(projectList));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponseForm<List<ProjectListResponse>>> getMyProjects(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("memberId");
        String userRole = (String) request.getAttribute("userRole").toString();
        List<ProjectListResponse> response = projectService.getMyProjects(userId, userRole);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<ProjectResponse>> getProject(@PathVariable Long projectId) {
        ProjectResponse response = projectService.getProject(projectId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> updateProject(@PathVariable Long projectId, @Valid @RequestBody ProjectCreateRequest request) {
        ProjectCreateResponse response = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 수정 성공"));
    }



    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> updateProjectStatus(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectStatusUpdateRequest request) {

        ProjectCreateResponse response = projectService.updateProjectStatus(projectId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 상태 변경 성공"));
    }
}
