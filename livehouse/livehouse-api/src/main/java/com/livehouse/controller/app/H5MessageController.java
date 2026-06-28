package com.livehouse.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.constants.MessageConstants;
import com.livehouse.common.result.Result;
import com.livehouse.entity.SysMessage;
import com.livehouse.service.SysMessageService;
import com.livehouse.util.JwtUtil;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/message")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5MessageController {

    private final SysMessageService sysMessageService;
    private final JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<PageResponse<SysMessage>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "readStatus", required = false) Integer readStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Page<SysMessage> page = sysMessageService.getReceivedMessagePage(
                userId, MessageConstants.RECEIVER_TYPE_H5, readStatus, pageNum, pageSize);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/unread/count")
    public Result<Long> unreadCount(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(sysMessageService.getUnreadCount(userId, MessageConstants.RECEIVER_TYPE_H5));
    }

    @PutMapping("/{messageId}/read")
    public Result<Void> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long messageId) {
        Long userId = getUserId(authorization);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return sysMessageService.markAsRead(messageId, userId, MessageConstants.RECEIVER_TYPE_H5)
                ? Result.success() : Result.error("标记已读失败");
    }

    private Long getUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        Claims claims = jwtUtil.parseToken(authorization.substring(7));
        if (claims == null || claims.get("userId") == null) {
            return null;
        }
        return Long.valueOf(claims.get("userId").toString());
    }
}
