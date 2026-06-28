package com.livehouse.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.dto.LiveInteractionRequest;
import com.livehouse.entity.HouseReserve;
import com.livehouse.entity.LiveInteraction;
import com.livehouse.entity.LiveSession;
import com.livehouse.service.LiveInteractionService;
import com.livehouse.service.LiveSessionService;
import com.livehouse.util.H5AuthUtil;
import com.livehouse.util.JwtUtil;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/h5/live")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5LiveController {

    private final LiveSessionService liveSessionService;
    private final LiveInteractionService liveInteractionService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<PageResponse<LiveSession>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {
        Page<LiveSession> page = liveSessionService.h5List(pageNum, pageSize, keyword, status);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(liveSessionService.h5Detail(id, false));
    }

    @PostMapping("/{id}/access")
    public Result<Map<String, Object>> access(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String password = payload.get("password") == null ? null : String.valueOf(payload.get("password"));
        String phone = payload.get("phone") == null ? null : String.valueOf(payload.get("phone"));
        Map<String, Object> data = liveSessionService.checkAccess(id, password, phone);
        Boolean granted = (Boolean) data.get("granted");
        return Boolean.TRUE.equals(granted) ? Result.success(data) : Result.error(403, String.valueOf(data.get("message")));
    }

    @PostMapping("/{id}/view")
    public Result<Void> view(@PathVariable Long id) {
        boolean success = liveSessionService.recordView(id);
        return success ? Result.success() : Result.error("记录观看失败");
    }

    @PostMapping("/{id}/reserve")
    public Result<HouseReserve> reserve(@PathVariable Long id,
                                        @RequestBody Map<String, Object> payload,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId != null) {
            payload.put("appUserId", userId);
        }
        HouseReserve reserve = liveSessionService.reserveFromLive(id, payload);
        return reserve == null ? Result.error("预约留资失败") : Result.success(reserve);
    }

    @GetMapping("/{id}/interaction")
    public Result<Map<String, Object>> interaction(@PathVariable Long id,
                                                   @RequestParam(value = "sinceId", required = false) Long sinceId,
                                                   @RequestParam(value = "limit", defaultValue = "30") int limit,
                                                   @RequestParam(value = "clientId", required = false) String clientId,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        return Result.success(liveInteractionService.getInteraction(id, userId, clientId, sinceId, limit));
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id,
                                            @RequestBody LiveInteractionRequest request,
                                            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        return Result.success(liveInteractionService.toggleLike(id, userId, request.getClientId(), request.getNickname()));
    }

    @PostMapping("/{id}/danmu")
    public Result<LiveInteraction> danmu(@PathVariable Long id,
                                         @RequestBody LiveInteractionRequest request,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        return Result.success(liveInteractionService.sendDanmu(id, userId, request.getClientId(), request.getNickname(), request.getContent()));
    }
}
