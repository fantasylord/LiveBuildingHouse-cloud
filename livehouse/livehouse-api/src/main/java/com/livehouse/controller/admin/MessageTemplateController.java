package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.SysMessageTemplate;
import com.livehouse.service.SysMessageTemplateService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message/template")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageTemplateController {

    private final SysMessageTemplateService templateService;

    @GetMapping({"", "/list"})
    public Result<PageResponse<SysMessageTemplate>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "msgType", required = false) Integer msgType,
            @RequestParam(value = "status", required = false) Integer status) {
        Page<SysMessageTemplate> page = templateService.listPage(pageNum, pageSize, keyword, msgType, status);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<SysMessageTemplate> detail(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    @PostMapping("")
    public Result<Void> add(@RequestBody SysMessageTemplate template) {
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        return templateService.save(template) ? Result.success() : Result.error("新增模板失败");
    }

    @PutMapping("")
    public Result<Void> update(@RequestBody SysMessageTemplate template) {
        return templateService.updateById(template) ? Result.success() : Result.error("更新模板失败");
    }

    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysMessageTemplate template = new SysMessageTemplate();
        template.setId(id);
        template.setStatus(status);
        return templateService.updateById(template) ? Result.success() : Result.error("更新模板状态失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return templateService.removeById(id) ? Result.success() : Result.error("删除模板失败");
    }
}
