package com.soda.global.log.data.domain;

import com.soda.global.log.data.dto.DataLogSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataLogCustomRepository {
    Page<DataLog> searchLogs(DataLogSearchRequest condition, Pageable pageable);
}
