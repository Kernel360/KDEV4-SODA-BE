package com.soda.request.repository;

import com.soda.request.dto.GetRequestCondition;
import com.soda.request.dto.request.GetMemberRequestCondition;
import com.soda.request.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepositoryCustom {
    Page<Request> searchByCondition(GetRequestCondition condition, Pageable pageable);

    Page<Request> searchByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable);
}