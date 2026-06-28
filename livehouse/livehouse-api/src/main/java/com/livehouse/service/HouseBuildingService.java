package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.HouseBuilding;

/**
 * 楼盘管理服务接口
 */
public interface HouseBuildingService extends IService<HouseBuilding> {

    /**
     * 分页查询楼盘列表
     */
    Page<HouseBuilding> listPage(int pageNum, int pageSize, String buildingName, String city, Integer status);

    /**
     * 获取楼盘详情
     */
    HouseBuilding getDetail(Long id);

    /**
     * 新增楼盘
     */
    boolean addBuilding(HouseBuilding building);

    /**
     * 更新楼盘
     */
    boolean updateBuilding(HouseBuilding building);

    /**
     * 删除楼盘
     */
    boolean deleteBuilding(Long id);

    /**
     * 修改楼盘状态
     */
    boolean changeStatus(Long id, Integer status);

    /**
     * 设置为私密楼盘
     */
    boolean setPrivate(Long id, Integer isPrivate);
}
