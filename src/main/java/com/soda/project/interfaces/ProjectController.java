package com.soda.project.interfaces;

import com.soda.global.response.ApiResponseForm;
import com.soda.project.application.ProjectFacade;
import com.soda.project.domain.ProjectService;
import com.soda.project.interfaces.dto.*;
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

    private final ProjectFacade projectFacade;
    private final ProjectService projectService;

    @PostMapping("")
    public ResponseEntity<ApiResponseForm<ProjectCreateResponse>> createProject(HttpServletRequest request,
                                                                                @Valid @RequestBody ProjectCreateRequest projectCreateRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectCreateResponse newProject = projectFacade.createProject(userRole, projectCreateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(newProject, "project 생성 성공"));
    }

    @PostMapping("/{projectId}/dev-companies")
    public ResponseEntity<ApiResponseForm<DevCompanyAssignmentResponse>> assignDevCompany(@PathVariable Long projectId,
                                                                                          HttpServletRequest request,
                                                                                          @Valid @RequestBody DevCompanyAssignmentRequest devCompanyAssignmentRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        DevCompanyAssignmentResponse response = projectFacade.assignDevCompany(projectId, userRole, devCompanyAssignmentRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "project에 개발사 지정 성공"));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponseForm<Page<ProjectListResponse>>> getAllProjects(@ModelAttribute ProjectSearchCondition projectSearchCondition,
                                                                                     @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectListResponse> projectList = projectFacade.getAllProjects(projectSearchCondition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(projectList));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponseForm<Page<MyProjectListResponse>>> getMyProjects(@ModelAttribute ProjectSearchCondition projectSearchCondition, HttpServletRequest request,
                                                                                      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = (Long) request.getAttribute("memberId");
        String userRole = (String) request.getAttribute("userRole").toString();
        Page<MyProjectListResponse> response = projectFacade.getMyProjects(projectSearchCondition, userId, userRole, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<ProjectViewResponse>> getProject(HttpServletRequest request, @PathVariable Long projectId) {
        Long userId = (Long) request.getAttribute("memberId");
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectViewResponse response = projectFacade.getProject(userId, userRole, projectId);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ApiResponseForm<ProjectStatusUpdateResponse>> updateProjectStatus(HttpServletRequest request, @PathVariable Long projectId,
                                                                                            @Valid @RequestBody ProjectStatusUpdateRequest updateRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        ProjectStatusUpdateResponse response = projectFacade.updateProjectStatus(userId, projectId, updateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 상태 변경 성공"));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(HttpServletRequest request, @PathVariable Long projectId) {
        String userRole = (String) request.getAttribute("userRole").toString();
        projectFacade.deleteProject(projectId, userRole);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponseForm<ProjectInfoUpdateResponse>> updateProjectInfo(HttpServletRequest request, @PathVariable Long projectId,
                                                                @Valid @RequestBody ProjectInfoUpdateRequest projectInfoUpdateRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectInfoUpdateResponse response = projectFacade.updateProjectInfo(userRole, projectId, projectInfoUpdateRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트 기본 정보 수정 성공"));
    }

    @PostMapping("/{projectId}/companies")
    public ResponseEntity<ApiResponseForm<ProjectCompanyAddResponse>> addCompanyToProject (HttpServletRequest request, @PathVariable Long projectId,
                                                                                           @Valid @RequestBody ProjectCompanyAddRequest projectCompanyAddRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectCompanyAddResponse response = projectFacade.addCompanyToProject(userRole, projectId, projectCompanyAddRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트에 새로운 회사/멤버 추가 성공"));
    }

    @DeleteMapping("/{projectId}/companies/{companyId}")
    public ResponseEntity<Void> deleteCompanyFromProject(@PathVariable Long projectId,
                                                         @PathVariable Long companyId, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole").toString();
        projectFacade.deleteCompanyFromProject(userRole, projectId, companyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponseForm<ProjectMemberAddResponse>> addMembersToProject (HttpServletRequest request, @PathVariable Long projectId,
                                                                   @Valid @RequestBody ProjectMemberAddRequest projectMemberAddRequest) {
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectMemberAddResponse response = projectFacade.addMemberToProject(userRole, projectId, projectMemberAddRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response, "프로젝트에 멤버 추가 성공"));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> deleteMemberFromProject (HttpServletRequest request, @PathVariable Long projectId, @PathVariable Long memberId) {
        String userRole = (String) request.getAttribute("userRole").toString();
        projectFacade.deleteMemberFromProject(userRole, projectId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiResponseForm<Page<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable Long projectId,
            @ModelAttribute ProjectMemberSearchCondition searchCondition,
            @PageableDefault(sort = "member.name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ProjectMemberResponse> memberPage = projectService.getProjectMembers(projectId, searchCondition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(memberPage));
    }

    @GetMapping("/my-company")
    public ResponseEntity<ApiResponseForm<Page<MyProjectListResponse>>> getMyCompanyProjects (HttpServletRequest request,
                                                                          @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = (Long) request.getAttribute("memberId");
        Page<MyProjectListResponse> response = projectFacade.getMyCompanyProjects(userId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }

    @GetMapping("/creation-trend")
    public ResponseEntity<ApiResponseForm<ProjectStatsResponse>> getProjectCreationTrend(
            HttpServletRequest request, // ADMIN 검증 등에 사용 가능
            @Valid @ModelAttribute ProjectStatsCondition statsRequest) {
        Long userId = (Long) request.getAttribute("memberId");
        String userRole = (String) request.getAttribute("userRole").toString();
        ProjectStatsResponse response = projectService.getProjectCreationTrend(userId, userRole, statsRequest);
        return ResponseEntity.ok(ApiResponseForm.success(response));
    }
    }
