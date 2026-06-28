package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.CustomerFollow;
import com.livehouse.service.CustomerFollowService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/follow")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerFollowController {

    private final CustomerFollowService customerFollowService;

    @GetMapping({"", "/list"})
    public Result<PageResponse<CustomerFollow>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "customerName", required = false) String customerName) {
        Page<CustomerFollow> page = customerFollowService.listPage(pageNum, pageSize, customerId, customerName);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<CustomerFollow> detail(@PathVariable Long id) {
        return Result.success(customerFollowService.getById(id));
    }

    @PostMapping("")
    public Result<Void> add(@RequestBody CustomerFollow follow) {
        return customerFollowService.save(follow) ? Result.success() : Result.error("新增跟进失败");
    }

    @PutMapping("")
    public Result<Void> update(@RequestBody CustomerFollow follow) {
        return customerFollowService.updateById(follow) ? Result.success() : Result.error("更新跟进失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return customerFollowService.removeById(id) ? Result.success() : Result.error("删除跟进失败");
    }
}
