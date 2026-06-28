package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.SmsRecord;
import com.livehouse.service.SmsService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SmsController {

    private final SmsService smsService;

    @GetMapping({"", "/list"})
    public Result<PageResponse<SmsRecord>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "status", required = false) Integer status) {
        Page<SmsRecord> page = smsService.listPage(pageNum, pageSize, phone, status);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<SmsRecord> detail(@PathVariable Long id) {
        return Result.success(smsService.getById(id));
    }

    @PostMapping("/send")
    public Result<Void> send(
            @RequestParam String phone,
            @RequestParam(required = false) String templateCode,
            @RequestParam String content) {
        return smsService.sendSms(phone, templateCode, content) ? Result.success() : Result.error("短信发送失败");
    }

    @PostMapping("/batch/send")
    public Result<Void> batchSend(
            @RequestBody String[] phones,
            @RequestParam(required = false) String templateCode,
            @RequestParam String content) {
        return smsService.batchSendSms(phones, templateCode, content) ? Result.success() : Result.error("批量发送失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return smsService.removeById(id) ? Result.success() : Result.error("删除记录失败");
    }
}