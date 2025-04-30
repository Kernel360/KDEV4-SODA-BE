package com.soda.project.infrastructure;

import com.soda.project.domain.stage.request.dto.GetRequestCondition;
import com.soda.project.domain.stage.request.dto.request.GetMemberRequestCondition;
import com.soda.project.domain.stage.request.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepositoryCustom {
    Page<Request> searchByCondition(Long projectId, GetRequestCondition condition, Pageable pageable);

    Page<Request> searchByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable);
}