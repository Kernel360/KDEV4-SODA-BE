package com.soda.global.log.dataLog.controller;

import com.soda.global.log.dataLog.domain.DataLog;
import com.soda.global.log.dataLog.dto.DataLogSearchRequest;
import com.soda.global.log.dataLog.dto.DataLogSearchResponse;
import com.soda.global.log.dataLog.service.DataLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("logs")
@RequiredArgsConstructor
public class DataLogController {

    private final DataLogService dataLogService;

    @GetMapping("")
    public DataLogSearchResponse<DataLog> getChangedLogs(DataLogSearchRequest condition, Pageable pageable) {
        return dataLogService.getChangeLogs(condition, pageable);
    }
}
