package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.HouseVr;

/**
 * VR素材管理服务接口
 */
public interface HouseVrService extends IService<HouseVr> {

    Page<HouseVr> listPage(int pageNum, int pageSize, Long buildingId, String buildingName, Long unitId, String vrName, Integer status);

    HouseVr getDetail(Long id);

    boolean addVr(HouseVr vr);

    boolean updateVr(HouseVr vr);

    boolean deleteVr(Long id);

    boolean changeStatus(Long id, Integer status);
}
