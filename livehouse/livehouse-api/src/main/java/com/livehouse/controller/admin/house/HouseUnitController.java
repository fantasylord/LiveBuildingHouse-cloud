package com.livehouse.controller.admin.house;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseUnit;
import com.livehouse.service.HouseUnitService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 户型管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/house/unit")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HouseUnitController {

    @Autowired
    private HouseUnitService houseUnitService;

    /**
     * 分页查询户型列表
     */
    @GetMapping("/list")
    public Result<PageResponse<HouseUnit>> listPage(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "buildingName", required = false) String buildingName,
            @RequestParam(value = "unitName", required = false) String unitName,
            @RequestParam(value = "status", required = false) Integer status) {
        
        Page<HouseUnit> page = houseUnitService.listPage(pageNum, pageSize, buildingId, buildingName, unitName, status);
        PageResponse<HouseUnit> pageResponse = PageUtil.buildPageResponse(page);
        return Result.success(pageResponse);
    }

    /**
     * 获取户型详情
     */
    @GetMapping("/{id}")
    public Result<HouseUnit> getDetail(@PathVariable Long id) {
        HouseUnit unit = houseUnitService.getDetail(id);
        return Result.success(unit);
    }

    /**
     * 新增户型
     */
    @PostMapping("")
    public Result<Void> addUnit(@RequestBody HouseUnit unit) {
        boolean success = houseUnitService.addUnit(unit);
        return success ? Result.success() : Result.error("新增户型失败");
    }

    /**
     * 更新户型
     */
    @PutMapping("")
    public Result<Void> updateUnit(@RequestBody HouseUnit unit) {
        boolean success = houseUnitService.updateUnit(unit);
        return success ? Result.success() : Result.error("更新户型失败");
    }

    /**
     * 删除户型
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUnit(@PathVariable Long id) {
        boolean success = houseUnitService.deleteUnit(id);
        return success ? Result.success() : Result.error("删除户型失败");
    }

    /**
     * 修改户型状态
     */
    @PutMapping("/{id}/changeStatus")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        boolean success = houseUnitService.changeStatus(id, status);
        return success ? Result.success() : Result.error("修改状态失败");
    }
}
