package com.soda.global.log.systemLog;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LogController {
    private final LogService logService;

//    @GetMapping("/test")
//    public ResponseEntity log() {
//        String test = "hi";
//        LogInfo logInfo = logService.findLogInfoByTest(test);
//
//        return new ResponseEntity(logInfo, HttpStatus.OK);
//    }
}
