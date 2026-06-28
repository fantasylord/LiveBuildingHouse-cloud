package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.entity.HouseUnit;
import com.livehouse.mapper.HouseBuildingMapper;
import com.livehouse.mapper.HouseUnitMapper;
import com.livehouse.service.HouseUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 户型管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HouseUnitServiceImpl extends ServiceImpl<HouseUnitMapper, HouseUnit> 
        implements HouseUnitService {

    private final HouseBuildingMapper houseBuildingMapper;

    @Override
    public Page<HouseUnit> listPage(int pageNum, int pageSize, Long buildingId, String buildingName, String unitName, Integer status) {
        Page<HouseUnit> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<HouseUnit> wrapper = new LambdaQueryWrapper<>();
        
        if (buildingId != null) {
            wrapper.eq(HouseUnit::getBuildingId, buildingId);
        }
        if (StringUtils.hasText(unitName)) {
            wrapper.like(HouseUnit::getUnitName, unitName);
        }
        if (status != null) {
            wrapper.eq(HouseUnit::getStatus, status);
        }
        
        wrapper.orderByDesc(HouseUnit::getCreateTime);
        
        Page<HouseUnit> resultPage = this.baseMapper.selectPage(page, wrapper);
        
        if (buildingName != null && !buildingName.isEmpty()) {
            List<HouseBuilding> buildings = houseBuildingMapper.selectList(new LambdaQueryWrapper<HouseBuilding>()
                    .like(HouseBuilding::getBuildingName, buildingName));
            if (!buildings.isEmpty()) {
                List<Long> buildingIds = buildings.stream()
                        .map(HouseBuilding::getId)
                        .collect(Collectors.toList());
                resultPage.setRecords(resultPage.getRecords().stream()
                        .filter(unit -> buildingIds.contains(unit.getBuildingId()))
                        .collect(Collectors.toList()));
                resultPage.setTotal((long) resultPage.getRecords().size());
            } else {
                resultPage.setRecords(Collections.emptyList());
                resultPage.setTotal(0);
            }
        }
        
        fillBuildingNames(resultPage.getRecords());
        
        return resultPage;
    }

    private void fillBuildingNames(List<HouseUnit> units) {
        if (units == null || units.isEmpty()) {
            return;
        }
        
        List<Long> buildingIds = units.stream()
                .map(HouseUnit::getBuildingId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (buildingIds.isEmpty()) {
            return;
        }
        
        List<HouseBuilding> buildings = houseBuildingMapper.selectBatchIds(buildingIds);
        
        Map<Long, String> buildingNameMap = new HashMap<>();
        for (HouseBuilding building : buildings) {
            buildingNameMap.put(building.getId(), building.getBuildingName());
        }
        
        for (HouseUnit unit : units) {
            if (unit.getBuildingId() != null) {
                unit.setBuildingName(buildingNameMap.get(unit.getBuildingId()));
            }
        }
    }

    @Override
    public HouseUnit getDetail(Long id) {
        HouseUnit unit = this.baseMapper.selectById(id);
        if (unit != null && unit.getBuildingId() != null) {
            HouseBuilding building = houseBuildingMapper.selectById(unit.getBuildingId());
            if (building != null) {
                unit.setBuildingName(building.getBuildingName());
            }
        }
        return unit;
    }

    @Override
    public boolean addUnit(HouseUnit unit) {
        if (unit == null) {
            return false;
        }
        return this.save(unit);
    }

    @Override
    public boolean updateUnit(HouseUnit unit) {
        if (unit == null || unit.getId() == null) {
            return false;
        }
        return this.updateById(unit);
    }

    @Override
    public boolean deleteUnit(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean changeStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        HouseUnit unit = new HouseUnit();
        unit.setId(id);
        unit.setStatus(status);
        return this.updateById(unit);
    }
}
