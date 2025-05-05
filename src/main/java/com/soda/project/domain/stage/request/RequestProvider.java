package com.soda.project.domain.stage.request;

import com.soda.project.domain.stage.request.dto.GetRequestCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestProvider {
    Request store(Request request);

    Page<Request> searchByCondition(Long projectId, GetRequestCondition condition, Pageable pageable);

}
