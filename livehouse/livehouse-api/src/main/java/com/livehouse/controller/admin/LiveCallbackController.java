package com.livehouse.controller.admin;

import com.livehouse.common.result.Result;
import com.livehouse.service.LiveSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/live/callback/tencent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class LiveCallbackController {

    private final LiveSessionService liveSessionService;

    @PostMapping("/stream")
    public Result<Void> streamCallback(@RequestBody Map<String, Object> payload) {
        boolean success = liveSessionService.handleStreamCallback(payload);
        return success ? Result.success() : Result.error("处理直播状态回调失败");
    }

    @PostMapping("/record")
    public Result<Void> recordCallback(@RequestBody Map<String, Object> payload) {
        boolean success = liveSessionService.handleRecordCallback(payload);
        return success ? Result.success() : Result.error("处理录制回调失败");
    }
}
