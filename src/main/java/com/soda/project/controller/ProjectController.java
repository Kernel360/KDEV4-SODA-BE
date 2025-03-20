package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.domain.ProjectCreateRequest;
import com.soda.project.domain.ProjectCreateResponse;
import com.soda.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        ProjectCreateResponse response = projectService.createProject(request);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }
}
