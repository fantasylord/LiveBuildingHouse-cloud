package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.HouseReserve;

public interface HouseReserveService extends IService<HouseReserve> {

    Page<HouseReserve> listPage(int pageNum, int pageSize, String customerName, String phone, Long buildingId, Integer visitStatus, Long liveSessionId);

    boolean changeStatus(Long id, Integer visitStatus);
}
