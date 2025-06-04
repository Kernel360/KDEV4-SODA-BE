package com.soda.project.infrastructure.stage.request;

import com.soda.project.domain.stage.request.Request;
import com.soda.project.interfaces.stage.request.dto.GetMemberRequestCondition;
import com.soda.project.interfaces.stage.request.dto.GetRequestCondition;
import com.soda.project.interfaces.stage.request.dto.RequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepositoryCustom {
    Page<Request> searchByCondition(Long projectId, GetRequestCondition condition, Pageable pageable);

    Page<RequestDTO> searchDtosByMemberCondition(Long memberId, GetMemberRequestCondition condition, Pageable pageable);
}