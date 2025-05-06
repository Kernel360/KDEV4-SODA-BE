package com.soda.project.domain.member;

import com.soda.project.domain.Project;

import java.util.List;
import java.util.Optional;

public interface MemberProjectProvider {
    List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId);

    Optional<MemberProject> findByProjectAndMemberIdAndIsDeletedFalse(Project project, Long memberId);
}
