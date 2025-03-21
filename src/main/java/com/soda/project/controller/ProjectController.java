package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.ProjectCreateRequest;
import com.soda.project.domain.ProjectCreateResponse;
import com.soda.project.domain.ProjectListResponse;
import com.soda.project.domain.ProjectResponse;
import com.soda.project.service.ProjectService;
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
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectCreateResponse response = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 생성 성공"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponseForm<List<ProjectListResponse>>> getAllProjects() {
        List<ProjectListResponse> projectList = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponseForm.success(projectList));
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
}
