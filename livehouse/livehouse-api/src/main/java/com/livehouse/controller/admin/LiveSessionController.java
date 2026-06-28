package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.dto.LiveStartNotifyRequest;
import com.livehouse.dto.LiveStartNotifyResult;
import com.livehouse.entity.LiveSession;
import com.livehouse.service.LiveSessionService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播场次管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/live/session")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LiveSessionController {

    @Autowired
    private LiveSessionService liveSessionService;

    /**
     * 分页查询直播场次列表
     */
    @GetMapping("/list")
    public Result<PageResponse<LiveSession>> listPage(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sessionName", required = false) String sessionName,
            @RequestParam(value = "liveType", required = false) Integer liveType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "platformId", required = false) Long platformId) {

        Page<LiveSession> page = liveSessionService.listPage(pageNum, pageSize, sessionName, liveType, status, platformId);
        PageResponse<LiveSession> pageResponse = PageUtil.buildPageResponse(page);
        return Result.success(pageResponse);
    }

    /**
     * 获取直播选项列表（用于配置联动）
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getOptions() {
        List<LiveSession> list = liveSessionService.list(new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getStatus, 0)
                .orderByAsc(LiveSession::getStartTime));

        List<Map<String, Object>> options = list.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("label", s.getSessionName());
            map.put("coverImage", s.getCoverImage());
            return map;
        }).toList();

        return Result.success(options);
    }

    /**
     * 获取直播场次详情
     */
    @GetMapping("/{id}")
    public Result<LiveSession> getDetail(@PathVariable Long id) {
        LiveSession session = liveSessionService.getDetail(id);
        return Result.success(session);
    }

    /**
     * 新增直播场次
     */
    @PostMapping("")
    public Result<Void> addSession(@RequestBody LiveSession session) {
        boolean success = liveSessionService.addSession(session);
        return success ? Result.success() : Result.error("新增直播场次失败");
    }

    /**
     * 更新直播场次
     */
    @PutMapping("")
    public Result<Void> updateSession(@RequestBody LiveSession session) {
        boolean success = liveSessionService.updateSession(session);
        return success ? Result.success() : Result.error("更新直播场次失败");
    }

    /**
     * 删除直播场次
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        boolean success = liveSessionService.deleteSession(id);
        return success ? Result.success() : Result.error("删除直播场次失败");
    }

    /**
     * 开播
     */
    @PostMapping("/{id}/start")
    public Result<Void> startLive(@PathVariable Long id) {
        boolean success = liveSessionService.startLive(id);
        return success ? Result.success() : Result.error("开播失败");
    }

    /**
     * 关播
     */
    @PostMapping("/{id}/notify-start")
    public Result<LiveStartNotifyResult> notifyStart(@PathVariable Long id, @RequestBody LiveStartNotifyRequest request) {
        return Result.success(liveSessionService.notifyStart(id, request));
    }

    @PostMapping("/{id}/stop")
    public Result<Void> stopLive(@PathVariable Long id) {
        boolean success = liveSessionService.stopLive(id);
        return success ? Result.success() : Result.error("关播失败");
    }

    /**
     * 修改直播状态
     */
    @PutMapping("/{id}/changeStatus")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        boolean success = liveSessionService.changeStatus(id, status);
        return success ? Result.success() : Result.error("修改状态失败");
    }

    @PostMapping("/{id}/sync-status")
    public Result<Void> syncStatus(@PathVariable Long id) {
        boolean success = liveSessionService.syncStatus(id);
        return success ? Result.success() : Result.error("同步直播状态失败");
    }

    @GetMapping("/{id}/push-info")
    public Result<Map<String, Object>> getPushInfo(@PathVariable Long id) {
        return Result.success(liveSessionService.getPushInfo(id));
    }

    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getStatistics(@PathVariable Long id) {
        return Result.success(liveSessionService.getStatistics(id));
    }

    @GetMapping("/replay/list")
    public Result<PageResponse<LiveSession>> replayList(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sessionName", required = false) String sessionName) {
        Page<LiveSession> page = liveSessionService.replayPage(pageNum, pageSize, sessionName);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @PutMapping("/{id}/replay")
    public Result<Void> updateReplay(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        String replayUrl = payload.get("replayUrl") == null ? null : String.valueOf(payload.get("replayUrl"));
        Integer replayStatus = null;
        if (payload.get("replayStatus") != null && !String.valueOf(payload.get("replayStatus")).isBlank()) {
            replayStatus = Integer.valueOf(String.valueOf(payload.get("replayStatus")));
        }
        boolean success = liveSessionService.updateReplay(id, replayUrl, replayStatus);
        return success ? Result.success() : Result.error("更新回放失败");
    }

    @DeleteMapping("/{id}/replay")
    public Result<Void> deleteReplay(@PathVariable Long id) {
        boolean success = liveSessionService.deleteReplay(id);
        return success ? Result.success() : Result.error("删除回放失败");
    }
}
