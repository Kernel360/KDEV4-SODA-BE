package com.soda.project.infrastructure;

import com.soda.project.domain.Project;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberProjectProviderImpl implements MemberProjectProvider {
    private final MemberProjectRepository memberProjectRepository;

    @Override
    public List<MemberProject> findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(Project project, Long companyId) {
        return memberProjectRepository.findAllByProjectAndMember_CompanyIdAndIsDeletedFalse(project, companyId);
    }

    @Override
    public Optional<MemberProject> findByProjectAndMemberIdAndIsDeletedFalse(Project project, Long memberId) {
        return memberProjectRepository.findByProjectAndMemberIdAndIsDeletedFalse(project, memberId);
    }
}
