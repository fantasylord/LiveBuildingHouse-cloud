package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.mapper.HouseBuildingMapper;
import com.livehouse.service.HouseBuildingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 楼盘管理服务实现类
 */
@Slf4j
@Service
public class HouseBuildingServiceImpl extends ServiceImpl<HouseBuildingMapper, HouseBuilding> 
        implements HouseBuildingService {

    @Override
    public Page<HouseBuilding> listPage(int pageNum, int pageSize, String buildingName, String city, Integer status) {
        Page<HouseBuilding> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<HouseBuilding> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(buildingName)) {
            wrapper.like(HouseBuilding::getBuildingName, buildingName);
        }
        if (StringUtils.hasText(city)) {
            wrapper.like(HouseBuilding::getCity, city);
        }
        if (status != null) {
            wrapper.eq(HouseBuilding::getStatus, status);
        }
        
        wrapper.eq(HouseBuilding::getDeleted, 0)
               .orderByDesc(HouseBuilding::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public HouseBuilding getDetail(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public boolean addBuilding(HouseBuilding building) {
        if (building == null) {
            return false;
        }
        return this.save(building);
    }

    @Override
    public boolean updateBuilding(HouseBuilding building) {
        if (building == null || building.getId() == null) {
            return false;
        }
        return this.updateById(building);
    }

    @Override
    public boolean deleteBuilding(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean changeStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        HouseBuilding building = new HouseBuilding();
        building.setId(id);
        building.setStatus(status);
        return this.updateById(building);
    }

    @Override
    public boolean setPrivate(Long id, Integer isPrivate) {
        if (id == null || isPrivate == null) {
            return false;
        }
        HouseBuilding building = new HouseBuilding();
        building.setId(id);
        building.setPrivated(isPrivate);
        return this.updateById(building);
    }
}
