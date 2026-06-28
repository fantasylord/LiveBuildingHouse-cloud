package com.livehouse.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseVr;
import com.livehouse.service.HouseVrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * H5端VR控制器
 */
@RestController
@RequestMapping("/api/h5/vr")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5VrController {

    private final HouseVrService houseVrService;

    /**
     * 获取VR详情
     */
    @GetMapping("/detail/{id}")
    public Result<HouseVr> detail(@PathVariable Long id) {
        HouseVr vr = houseVrService.getById(id);
        if (vr == null) {
            return Result.error(404, "VR素材不存在");
        }
        if (vr.getPrivated() != null && vr.getPrivated() == 1) {
            return Result.error(403, "该VR素材为私密资源");
        }
        return Result.success(vr);
    }

    /**
     * 获取VR列表（支持按楼盘ID或户型ID筛选）
     */
    @GetMapping("/list")
    public Result<List<HouseVr>> list(
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "unitId", required = false) Long unitId) {

        LambdaQueryWrapper<HouseVr> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseVr::getPrivated, 0);

        if (buildingId != null) {
            wrapper.eq(HouseVr::getBuildingId, buildingId);
        }
        if (unitId != null) {
            wrapper.eq(HouseVr::getUnitId, unitId);
        }

        wrapper.orderByAsc(HouseVr::getSortOrder);
        List<HouseVr> list = houseVrService.list(wrapper);
        return Result.success(list);
    }
}
