package com.livehouse.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.entity.HouseUnit;
import com.livehouse.entity.HouseVr;
import com.livehouse.service.HouseBuildingService;
import com.livehouse.service.HouseUnitService;
import com.livehouse.service.HouseVrService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/h5/house")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5HouseController {

    private final HouseBuildingService houseBuildingService;
    private final HouseUnitService houseUnitService;
    private final HouseVrService houseVrService;

    @GetMapping("/list")
    public Result<PageResponse<HouseBuilding>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
            @RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
            @RequestParam(value = "areaMin", required = false) BigDecimal areaMin,
            @RequestParam(value = "areaMax", required = false) BigDecimal areaMax,
            @RequestParam(value = "roomCount", required = false) Integer roomCount,
            @RequestParam(value = "sortField", required = false) String sortField) {

        LambdaQueryWrapper<HouseBuilding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseBuilding::getStatus, 1);
        wrapper.eq(HouseBuilding::getPrivated, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(HouseBuilding::getBuildingName, keyword);
        }
        if (city != null && !city.isEmpty()) {
            wrapper.eq(HouseBuilding::getCity, city);
        }
        if (priceMin != null) {
            wrapper.ge(HouseBuilding::getMinPrice, priceMin);
        }
        if (priceMax != null) {
            wrapper.le(HouseBuilding::getMaxPrice, priceMax);
        }

        if ("price".equals(sortField)) {
            wrapper.orderByAsc(HouseBuilding::getAvgPrice);
        } else if ("area".equals(sortField)) {
            wrapper.orderByDesc(HouseBuilding::getTotalUnits);
        } else {
            wrapper.orderByDesc(HouseBuilding::getSortOrder);
            wrapper.orderByDesc(HouseBuilding::getCreateTime);
        }

        Page<HouseBuilding> page = houseBuildingService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        HouseBuilding building = houseBuildingService.getById(id);
        if (building == null) {
            return Result.error(404, "楼盘不存在");
        }

        List<HouseUnit> units = houseUnitService.list(
                new LambdaQueryWrapper<HouseUnit>()
                        .eq(HouseUnit::getBuildingId, id)
                        .eq(HouseUnit::getStatus, 1)
        );

        List<HouseVr> vrList = houseVrService.list(
                new LambdaQueryWrapper<HouseVr>()
                        .eq(HouseVr::getBuildingId, id)
                        .eq(HouseVr::getPrivated, 0)
                        .orderByAsc(HouseVr::getSortOrder)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("building", building);
        result.put("units", units);
        result.put("vrList", vrList);

        return Result.success(result);
    }

    @GetMapping("/hot")
    public Result<List<HouseBuilding>> hotList() {
        Page<HouseBuilding> page = houseBuildingService.page(new Page<>(1, 6),
                new LambdaQueryWrapper<HouseBuilding>()
                        .eq(HouseBuilding::getStatus, 1)
                        .eq(HouseBuilding::getPrivated, 0)
                        .orderByDesc(HouseBuilding::getViewCount)
                        .orderByDesc(HouseBuilding::getSortOrder)
        );
        return Result.success(page.getRecords());
    }

    @GetMapping("/cities")
    public Result<List<String>> getCities() {
        List<String> cities = houseBuildingService.listObjs(
                new LambdaQueryWrapper<HouseBuilding>()
                        .eq(HouseBuilding::getStatus, 1)
                        .select(HouseBuilding::getCity),
                obj -> (String) obj
        ).stream().distinct().toList();
        return Result.success(cities);
    }
}