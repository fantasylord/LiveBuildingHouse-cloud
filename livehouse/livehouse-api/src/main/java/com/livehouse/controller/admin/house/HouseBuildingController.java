package com.livehouse.controller.admin.house;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.service.HouseBuildingService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 楼盘管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/house/building")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HouseBuildingController {

    @Autowired
    private HouseBuildingService houseBuildingService;

    /**
     * 分页查询楼盘列表
     */
    @GetMapping("/list")
    public Result<PageResponse<HouseBuilding>> listPage(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "buildingName", required = false) String buildingName,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "status", required = false) Integer status) {

        Page<HouseBuilding> page = houseBuildingService.listPage(pageNum, pageSize, buildingName, city, status);
        PageResponse<HouseBuilding> pageResponse = PageUtil.buildPageResponse(page);
        return Result.success(pageResponse);
    }

    /**
     * 获取楼盘选项列表（用于配置联动）
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getOptions() {
        List<HouseBuilding> list = houseBuildingService.list(new LambdaQueryWrapper<HouseBuilding>()
                .eq(HouseBuilding::getStatus, 1)
                .eq(HouseBuilding::getPrivated, 0)
                .orderByAsc(HouseBuilding::getSortOrder)
                .orderByDesc(HouseBuilding::getCreateTime));

        List<Map<String, Object>> options = list.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("label", b.getBuildingName());
            map.put("coverImage", b.getCoverImage());
            return map;
        }).toList();

        return Result.success(options);
    }

    /**
     * 获取楼盘详情
     */
    @GetMapping("/{id}")
    public Result<HouseBuilding> getDetail(@PathVariable Long id) {
        HouseBuilding building = houseBuildingService.getDetail(id);
        return Result.success(building);
    }

    /**
     * 新增楼盘
     */
    @PostMapping("")
    public Result<Void> addBuilding(@RequestBody HouseBuilding building) {
        boolean success = houseBuildingService.addBuilding(building);
        return success ? Result.success() : Result.error("新增楼盘失败");
    }

    /**
     * 更新楼盘
     */
    @PutMapping("")
    public Result<Void> updateBuilding(@RequestBody HouseBuilding building) {
        boolean success = houseBuildingService.updateBuilding(building);
        return success ? Result.success() : Result.error("更新楼盘失败");
    }

    /**
     * 删除楼盘
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBuilding(@PathVariable Long id) {
        boolean success = houseBuildingService.deleteBuilding(id);
        return success ? Result.success() : Result.error("删除楼盘失败");
    }

    /**
     * 修改楼盘状态
     */
 @PutMapping("/{id}/changeStatus")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        boolean success = houseBuildingService.changeStatus(id, status);
        return success ? Result.success() : Result.error("修改状态失败");
    }

    /**
     * 设置为私密楼盘
     */
    @PutMapping("/{id}/setPrivate")
    public Result<Void> setPrivate(
            @PathVariable Long id,
            @RequestParam Integer isPrivate) {
        boolean success = houseBuildingService.setPrivate(id, isPrivate);
        return success ? Result.success() : Result.error("设置失败");
    }
}
