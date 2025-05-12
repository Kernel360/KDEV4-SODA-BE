package com.soda.project.infrastructure.stage.request;

import com.soda.project.interfaces.stage.request.dto.GetRequestCondition;
import com.soda.project.interfaces.stage.request.dto.GetMemberRequestCondition;
import com.soda.project.domain.stage.request.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepositoryCustom {
    Page<Request> searchByCondition(Long projectId, GetRequestCondition condition, Pageable pageable);

    Page<Request> searchByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable);
}