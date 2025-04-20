package com.soda.project.repository;

import com.soda.member.enums.MemberProjectRole;
import com.soda.project.entity.MemberProject;
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
