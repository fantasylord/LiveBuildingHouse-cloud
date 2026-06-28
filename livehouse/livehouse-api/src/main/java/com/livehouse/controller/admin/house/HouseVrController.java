package com.livehouse.controller.admin.house;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseVr;
import com.livehouse.service.HouseVrService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VR素材管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/house/vr")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HouseVrController {

    @Autowired
    private HouseVrService houseVrService;

    @GetMapping("/list")
    public Result<PageResponse<HouseVr>> listPage(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "buildingName", required = false) String buildingName,
            @RequestParam(value = "unitId", required = false) Long unitId,
            @RequestParam(value = "vrName", required = false) String vrName,
            @RequestParam(value = "status", required = false) Integer status) {

        Page<HouseVr> page = houseVrService.listPage(pageNum, pageSize, buildingId, buildingName, unitId, vrName, status);
        PageResponse<HouseVr> pageResponse = PageUtil.buildPageResponse(page);
        return Result.success(pageResponse);
    }

    /**
     * 获取VR选项列表（用于配置联动）
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getOptions() {
        List<HouseVr> list = houseVrService.list(new LambdaQueryWrapper<HouseVr>()
                .eq(HouseVr::getStatus, 1)
                .eq(HouseVr::getPrivated, 0)
                .orderByAsc(HouseVr::getSortOrder));

        List<Map<String, Object>> options = list.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("label", v.getVrName());
            map.put("coverImage", v.getThumbnailUrl());
            return map;
        }).toList();

        return Result.success(options);
    }

    @GetMapping("/{id}")
    public Result<HouseVr> getDetail(@PathVariable Long id) {
        HouseVr vr = houseVrService.getDetail(id);
        return Result.success(vr);
    }

    @PostMapping("")
    public Result<Void> addVr(@RequestBody HouseVr vr) {
        boolean success = houseVrService.addVr(vr);
        return success ? Result.success() : Result.error("新增VR素材失败");
    }

    @PutMapping("")
    public Result<Void> updateVr(@RequestBody HouseVr vr) {
        boolean success = houseVrService.updateVr(vr);
        return success ? Result.success() : Result.error("更新VR素材失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteVr(@PathVariable Long id) {
        boolean success = houseVrService.deleteVr(id);
        return success ? Result.success() : Result.error("删除VR素材失败");
    }

    @PutMapping("/{id}/changeStatus")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        boolean success = houseVrService.changeStatus(id, status);
        return success ? Result.success() : Result.error("修改状态失败");
    }
}
