package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.service.CustomerInfoService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    private final CustomerInfoService customerInfoService;

    @GetMapping({"", "/list"})
    public Result<PageResponse<CustomerInfo>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "customerSource", required = false) String customerSource) {
        Page<CustomerInfo> page = customerInfoService.listPage(pageNum, pageSize, customerName, phone, customerSource);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<CustomerInfo> detail(@PathVariable Long id) {
        return Result.success(customerInfoService.getById(id));
    }

    @PostMapping("")
    public Result<Void> add(@RequestBody CustomerInfo customer) {
        return customerInfoService.addCustomer(customer) ? Result.success() : Result.error("新增客户失败，手机号可能已存在");
    }

    @PutMapping("")
    public Result<Void> update(@RequestBody CustomerInfo customer) {
        return customerInfoService.updateCustomer(customer) ? Result.success() : Result.error("更新客户失败");
    }

    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long consultantId) {
        return customerInfoService.assignCustomer(id, consultantId) ? Result.success() : Result.error("分配客户失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return customerInfoService.removeById(id) ? Result.success() : Result.error("删除客户失败");
    }
}
