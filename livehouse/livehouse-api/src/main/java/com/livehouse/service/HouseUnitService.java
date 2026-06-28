package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.HouseUnit;

/**
 * 户型管理服务接口
 */
public interface HouseUnitService extends IService<HouseUnit> {

    /**
     * 分页查询户型列表
     */
    Page<HouseUnit> listPage(int pageNum, int pageSize, Long buildingId, String buildingName, String unitName, Integer status);

    /**
     * 获取户型详情
     */
    HouseUnit getDetail(Long id);

    /**
     * 新增户型
     */
    boolean addUnit(HouseUnit unit);

    /**
     * 更新户型
     */
    boolean updateUnit(HouseUnit unit);

    /**
     * 删除户型
     */
    boolean deleteUnit(Long id);

    /**
     * 修改户型状态
     */
    boolean changeStatus(Long id, Integer status);
}
