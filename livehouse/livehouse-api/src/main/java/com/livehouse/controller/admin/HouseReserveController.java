package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseReserve;
import com.livehouse.service.HouseReserveService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reserve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class HouseReserveController {

    private final HouseReserveService houseReserveService;

    @GetMapping({"", "/list"})
    public Result<PageResponse<HouseReserve>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "visitStatus", required = false) Integer visitStatus,
            @RequestParam(value = "liveSessionId", required = false) Long liveSessionId) {
        Page<HouseReserve> page = houseReserveService.listPage(pageNum, pageSize, customerName, phone, buildingId, visitStatus, liveSessionId);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<HouseReserve> detail(@PathVariable Long id) {
        return Result.success(houseReserveService.getById(id));
    }

    @PostMapping("")
    public Result<Void> add(@RequestBody HouseReserve reserve) {
        return houseReserveService.save(reserve) ? Result.success() : Result.error("新增预约失败");
    }

    @PutMapping("")
    public Result<Void> update(@RequestBody HouseReserve reserve) {
        return houseReserveService.updateById(reserve) ? Result.success() : Result.error("更新预约失败");
    }

    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer visitStatus) {
        return houseReserveService.changeStatus(id, visitStatus) ? Result.success() : Result.error("更新预约状态失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return houseReserveService.removeById(id) ? Result.success() : Result.error("删除预约失败");
    }
}
