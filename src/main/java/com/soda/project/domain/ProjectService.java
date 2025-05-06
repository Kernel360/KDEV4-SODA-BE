package com.soda.project.domain;

import com.querydsl.core.Tuple;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.interfaces.dto.ProjectListResponse;
import com.soda.project.interfaces.dto.ProjectSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectProvider projectProvider;

    /**
     * 프로젝트를 생성하는 메서드
     */
    public Project createAndStoreProject(
            String title, String description, LocalDateTime startDate, LocalDateTime endDate,
            List<Company> clientCompanies, List<Member> clientManagers, List<Member> clientMembers,
            List<String> initialStageNames) {

        log.debug("ProjectService: 프로젝트 생성 및 저장 시작");

        Project project = Project.create(
                title, description, startDate, endDate,
                clientCompanies, clientManagers, clientMembers,
                initialStageNames
        );

        Project savedProject = projectProvider.store(project);
        log.info("ProjectService: 프로젝트 저장 완료: projectId={}", savedProject.getId());
        return savedProject;
    }

    /**
     * 기존 프로젝트에 개발사 및 해당 개발사의 담당자/참여자를 지정하는 메서드
     */
    public void assignDevCompanyAndMembers(Project project, List<Company> devCompanies, List<Member> devManagers, List<Member> devMembers) {
        log.info("[ProjectService] 개발사 및 멤버 할당 시작: projectId={}", project.getId());
        // 1. Project 엔티티 메서드 호출하여 연관관계 설정 (메모리)
        project.assignDevCompanies(devCompanies);
        project.assignDevMembers(devManagers, devMembers);
        // 2. 변경된 Project 엔티티 저장
        projectProvider.store(project);
        log.info("[ProjectService] 개발사 및 멤버 할당 및 저장 완료: projectId={}", project.getId());
    }

    /**
     * 프로젝트 상태 변경
     */
    public void changeProjectStatus(Project project, ProjectStatus newStatus) {
        log.info("프로젝트 상태 변경 시도: ID={}, 현재 상태={}, 변경 상태={}",
                project.getId(), project.getStatus(), newStatus);
        project.changeStatus(newStatus);
        projectProvider.store(project);
        log.info("프로젝트 상태 변경 완료: ID={}, 새 상태={}", project.getId(), project.getStatus());
    }

    /**
     * 프로젝트 기본 정보 수정
     */
    public void updateProjectInfo(Project project, String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("프로젝트 정보 수정 시작: projectId={}, title={}", project.getId(), title);
        project.updateProjectInfo(title, description, startDate, endDate);
        projectProvider.store(project);
        log.info("프로젝트 정보 수정 완료: projectId={}", project.getId());
    }

    /**
     * 전체 프로젝트 목록 조회하는 메서드
     */
    public Page<ProjectListResponse> getAllProjects(ProjectSearchCondition condition, Pageable pageable) {
        return projectProvider.searchProjects(condition, pageable);
    }

    /**
     * 특정 사용자가 참여한 프로젝트 목록 조회 메서드
     */
    public Page<Tuple> findMyProjectsData(ProjectSearchCondition condition, Long userId, Pageable pageable) {
        return projectProvider.findMyProjectsData(condition, userId, pageable);
    }

    /**
     * 사용자의 회사가 참여한 프로젝트 목록 조회
     */
    public Page<Tuple> findMyCompanyProjectsData(Long userId, Long companyId, Pageable pageable) {
        return projectProvider.findMyCompanyProjectsData(userId, companyId, pageable);
    }

    /**
     * 프로젝트 삭제
     */
    public void deleteProject(Project project) {
        projectProvider.delete(project);
        log.info("ProjectService: 프로젝트 삭제 완료: projectId={}", project.getId());
    }

    /**
     * 프로젝트에 회사 및 멤버 추가
     */
    public void addCompanyAndMembersToProject(Project project, Company company, CompanyProjectRole companyRole,
                                              List<Member> managers, List<Member> members) {
        log.info("[ProjectService] 회사({}) 및 멤버 추가 시작: projectId={}, role={}",
                company.getName(), project.getId(), companyRole);

        // 1. 역할에 따라 Project 엔티티 메서드 호출하여 연관관계 설정
        if (companyRole == CompanyProjectRole.CLIENT_COMPANY) {
            project.assignClientCompanies(List.of(company));
            project.assignClientMembers(managers, members);
        } else if (companyRole == CompanyProjectRole.DEV_COMPANY) {
            project.assignDevCompanies(List.of(company));
            project.assignDevMembers(managers, members);
        } else {
            log.error("지원하지 않는 회사 역할입니다: {}", companyRole);
        }
        // 2. 변경된 Project 엔티티 저장
        projectProvider.store(project);
        log.info("[ProjectService] 회사({}) 및 멤버 추가 및 저장 완료: projectId={}, role={}",
                company.getName(), project.getId(), companyRole);
    }

    public Project getValidProject(Long projectId) {
        return projectProvider.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> {
                    log.error("프로젝트를 찾을 수 없음: 프로젝트 ID = {}", projectId);
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });
    }
}
