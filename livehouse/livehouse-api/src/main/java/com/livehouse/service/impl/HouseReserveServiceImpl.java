package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.HouseReserve;
import com.livehouse.mapper.HouseReserveMapper;
import com.livehouse.service.HouseReserveService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HouseReserveServiceImpl extends ServiceImpl<HouseReserveMapper, HouseReserve> implements HouseReserveService {

    @Override
    public Page<HouseReserve> listPage(int pageNum, int pageSize, String customerName, String phone, Long buildingId, Integer visitStatus, Long liveSessionId) {
        LambdaQueryWrapper<HouseReserve> wrapper = new LambdaQueryWrapper<HouseReserve>()
                .eq(HouseReserve::getDeleted, 0)
                .orderByDesc(HouseReserve::getCreateTime);
        if (StringUtils.hasText(customerName)) {
            wrapper.like(HouseReserve::getCustomerName, customerName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(HouseReserve::getPhone, phone);
        }
        if (buildingId != null) {
            wrapper.eq(HouseReserve::getBuildingId, buildingId);
        }
        if (visitStatus != null) {
            wrapper.eq(HouseReserve::getVisitStatus, visitStatus);
        }
        if (liveSessionId != null) {
            wrapper.eq(HouseReserve::getLiveSessionId, liveSessionId);
        }
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean changeStatus(Long id, Integer visitStatus) {
        if (id == null || visitStatus == null) {
            return false;
        }
        HouseReserve reserve = new HouseReserve();
        reserve.setId(id);
        reserve.setVisitStatus(visitStatus);
        return this.updateById(reserve);
    }
}
