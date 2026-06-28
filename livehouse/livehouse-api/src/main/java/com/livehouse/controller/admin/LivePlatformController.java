package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.LivePlatform;
import com.livehouse.service.LivePlatformService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 三方直播平台配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/live/platform")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LivePlatformController {

    @Autowired
    private LivePlatformService livePlatformService;

    /**
     * 分页查询直播平台列表
     */
    @GetMapping("/list")
    public Result<PageResponse<LivePlatform>> listPage(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "platformName", required = false) String platformName,
            @RequestParam(value = "platformType", required = false) String platformType,
            @RequestParam(value = "status", required = false) Integer status) {
        
        Page<LivePlatform> page = livePlatformService.listPage(pageNum, pageSize, platformName, platformType, status);
        PageResponse<LivePlatform> pageResponse = PageUtil.buildPageResponse(page);
        return Result.success(pageResponse);
    }

    /**
     * 获取直播平台列表（不分页）
     */
    @GetMapping("/options")
    public Result<List<LivePlatform>> getOptions(
            @RequestParam(value = "status", required = false) Integer status) {
        List<LivePlatform> list = livePlatformService.getList(status);
        return Result.success(list);
    }

    /**
     * 获取直播平台详情
     */
    @GetMapping("/{id}")
    public Result<LivePlatform> getDetail(@PathVariable Long id) {
        LivePlatform platform = livePlatformService.getDetail(id);
        return Result.success(platform);
    }

    /**
     * 新增直播平台
     */
    @PostMapping("")
    public Result<Void> addPlatform(@RequestBody LivePlatform platform) {
        boolean success = livePlatformService.addPlatform(platform);
        return success ? Result.success() : Result.error("新增直播平台失败");
    }

    /**
     * 更新直播平台
     */
    @PutMapping("")
    public Result<Void> updatePlatform(@RequestBody LivePlatform platform) {
        boolean success = livePlatformService.updatePlatform(platform);
        return success ? Result.success() : Result.error("更新直播平台失败");
    }

    /**
     * 删除直播平台
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePlatform(@PathVariable Long id) {
        boolean success = livePlatformService.deletePlatform(id);
        return success ? Result.success() : Result.error("删除直播平台失败");
    }
}