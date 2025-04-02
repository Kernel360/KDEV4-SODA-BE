package com.soda.project;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.create.ProjectCreateService;
import com.soda.project.delete.ProjectDeleteService;
import com.soda.project.search.ProjectSearchService;
import com.soda.project.update.ProjectUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectSearchService projectSearchService;
    private final ProjectCreateService projectCreateService;
    private final ProjectUpdateService projectUpdateService;
    private final ProjectDeleteService projectDeleteService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ProjectResponse>> createProject(@RequestBody ProjectRequest request) {
        ProjectResponse response = projectCreateService.createProject(request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 생성 성공"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponseForm<List<ProjectListResponse>>> getAllProjects() {
        List<ProjectListResponse> projectList = projectSearchService.getAllProjects();
        return ResponseEntity.ok(ApiResponseForm.success(projectList));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<ProjectResponse>> getProject(@PathVariable Long projectId) {
        ProjectResponse response = projectSearchService.getProject(projectId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectDeleteService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<ProjectResponse>> updateProject(@PathVariable Long projectId, @RequestBody ProjectRequest request) {
        ProjectResponse response = projectUpdateService.updateProject(projectId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 수정 성공"));
    }
}
