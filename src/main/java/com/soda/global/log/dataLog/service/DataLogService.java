package com.soda.global.log.dataLog.service;

import com.soda.global.log.dataLog.domain.DataLog;
import com.soda.global.log.dataLog.domain.DataLogRepository;
import com.soda.global.log.dataLog.dto.DataLogSearchRequest;
import com.soda.global.log.dataLog.dto.DataLogSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataLogService {

    private final DataLogRepository dataLogRepository;

    public DataLogSearchResponse<DataLog> getChangeLogs(DataLogSearchRequest condition, Pageable pageable) {
        return DataLogSearchResponse.from(dataLogRepository.searchLogs(condition, pageable));
    }
}
