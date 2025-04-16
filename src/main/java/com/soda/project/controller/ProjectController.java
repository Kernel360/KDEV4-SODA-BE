package com.soda.project.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.dto.ProjectCreateRequest;
import com.soda.project.dto.ProjectCreateResponse;
import com.soda.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> createProject(HttpServletRequest request,
                                                                                @Valid @RequestBody ProjectCreateRequest projectCreateRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectCreateResponse newProject = projectService.createProject(userRole, projectCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(newProject, "project 생성 성공"));
    }
}
