package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.dto.WhitelistBatchImportDTO;
import com.livehouse.dto.WhitelistCustomerImportDTO;
import com.livehouse.entity.LiveWhitelist;
import com.livehouse.service.LiveWhitelistService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import com.livehouse.vo.WhitelistBatchImportResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 直播白名单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/live/whitelist")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LiveWhitelistController {

    @Autowired
    private LiveWhitelistService liveWhitelistService;

    /**
     * 分页查询白名单列表
     */
    @GetMapping("/list")
    public Result<PageResponse<LiveWhitelist>> listPage(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "phone", required = false) String phone) {
        
        Page<LiveWhitelist> page = liveWhitelistService.listPage(pageNum, pageSize, sessionId, phone);
        PageResponse<LiveWhitelist> pageResponse = PageUtil.buildPageResponse(page);
        return Result.success(pageResponse);
    }

    /**
     * 获取白名单详情
     */
    @GetMapping("/{id}")
    public Result<LiveWhitelist> getDetail(@PathVariable Long id) {
        LiveWhitelist whitelist = liveWhitelistService.getDetail(id);
        return Result.success(whitelist);
    }

    /**
     * 新增白名单
     */
    @PostMapping("")
    public Result<Void> addWhitelist(@RequestBody LiveWhitelist whitelist) {
        boolean success = liveWhitelistService.addWhitelist(whitelist);
        return success ? Result.success() : Result.error("添加白名单失败");
    }

    /**
     * 批量添加白名单
     */
    @PostMapping("/batch")
    public Result<WhitelistBatchImportResult> batchAddWhitelist(@RequestBody WhitelistBatchImportDTO dto) {
        WhitelistBatchImportResult result = liveWhitelistService.batchImport(dto.getSessionId(), dto.getContent());
        return Result.success(result);
    }

    @PostMapping("/batch/customers")
    public Result<WhitelistBatchImportResult> batchAddCustomers(@RequestBody WhitelistCustomerImportDTO dto) {
        WhitelistBatchImportResult result = liveWhitelistService.batchImportCustomers(dto.getSessionId(), dto.getCustomerIds(), dto.getRemark());
        return Result.success(result);
    }

    /**
     * 删除白名单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteWhitelist(@PathVariable Long id) {
        boolean success = liveWhitelistService.deleteWhitelist(id);
        return success ? Result.success() : Result.error("删除白名单失败");
    }

    /**
     * 检查手机号是否在白名单中
     */
    @GetMapping("/check")
    public Result<Boolean> checkWhitelist(
            @RequestParam Long sessionId,
            @RequestParam String phone) {
        boolean inWhitelist = liveWhitelistService.isInWhitelist(sessionId, phone);
        return Result.success(inWhitelist);
    }
}
