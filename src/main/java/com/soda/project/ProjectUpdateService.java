package com.soda.project;

import com.soda.global.response.GeneralException;
import com.soda.member.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectUpdateService {
    private final ProjectRepository projectRepository;
    private final CompanyService companyService;
    private final MemberService memberService;

    /*
        프로젝트 수정하기
        - 기본 정보 수정
        - 개발사 수정, 관리자/직원 수정
        - 고객사 수정, 관리자/직원 수정
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        // 1. 프로젝트 존재 여부 체크
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        var devCompany = companyService.getCompanyByIdByBaki(request.getDevCompanyId());
        var clientCompany = companyService.getCompanyByIdByBaki(request.getClientCompanyId());

        var devManagers = memberService.findByIds(request.getDevManagers());
        var devMembers = memberService.findByIds(request.getDevMembers());
        var clientManagers = memberService.findByIds(request.getClientManagers());
        var clientMembers = memberService.findByIds(request.getClientMembers());

        project.updateProjectInfo(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate(),
                devCompany, clientCompany, devManagers, devMembers, clientManagers, clientMembers);
        projectRepository.save(project);

        return project.toResponse();
    }
}
