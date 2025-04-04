package com.soda.global.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;

//    public LogInfo findLogInfoByTest(String test) {
//        return logRepository.findLogInfoByTest(test);
//    }
}
