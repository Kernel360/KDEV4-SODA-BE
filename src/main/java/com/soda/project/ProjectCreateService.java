package com.soda.project;

import com.soda.global.response.GeneralException;
import com.soda.member.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectCreateService {

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final CompanyService companyService;

    /*
        프로젝트 생성하기
        - 기본 정보 생성
        - 개발사 지정, 관리자/직원 지정
        - 고객사 지정, 관리자/직원 지정
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        // 1. 프로젝트 제목 중복 체크
        if (projectRepository.existsByTitle(request.getTitle())) {
            throw new GeneralException(ProjectErrorCode.PROJECT_TITLE_DUPLICATED);
        }

        var devCompany = companyService.getCompanyByIdByBaki(request.getDevCompanyId());
        var clientCompany = companyService.getCompanyByIdByBaki(request.getClientCompanyId());

        var devManagers = memberService.findByIds(request.getDevManagers());
        var devMembers = memberService.findByIds(request.getDevMembers());
        var clientManagers = memberService.findByIds(request.getClientManagers());
        var clientMembers = memberService.findByIds(request.getClientMembers());

        Project project = Project.create(request, devCompany, clientCompany, devManagers, devMembers, clientManagers, clientMembers);
        projectRepository.save(project);

        // 4. response DTO 생성
        return project.toResponse();
    }
}
