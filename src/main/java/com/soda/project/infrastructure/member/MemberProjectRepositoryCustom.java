package com.soda.project.infrastructure.member;

import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.domain.member.MemberProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberProjectRepositoryCustom {

    Page<MemberProject> findFilteredMembers(Long projectId,
                                            List<Long> companyIds,
                                            Long companyId,
                                            MemberProjectRole memberRole,
                                            Long memberId,
                                            Pageable pageable);
}
