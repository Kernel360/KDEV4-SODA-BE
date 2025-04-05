package com.soda.global.log.dataLog.service;

import com.soda.global.log.dataLog.domain.DataLog;
import com.soda.global.log.dataLog.domain.DataLogRepository;
import com.soda.global.log.dataLog.dto.DataLogSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataLogService {

    private final DataLogRepository dataLogRepository;

    public Page<DataLog> getChangeLogs(DataLogSearchRequest condition, Pageable pageable) {
        return dataLogRepository.searchLogs(condition, pageable);
    }
}
