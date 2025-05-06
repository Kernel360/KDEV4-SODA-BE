package com.soda.project.domain.member;

import com.soda.project.domain.Project;

import java.util.List;

public interface MemberProjectProvider {
    List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId);
}
