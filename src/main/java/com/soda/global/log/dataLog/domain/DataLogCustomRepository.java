package com.soda.global.log.dataLog.domain;

import com.soda.global.log.dataLog.dto.DataLogSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataLogCustomRepository {
    Page<DataLog> searchLogs(DataLogSearchRequest condition, Pageable pageable);
}
