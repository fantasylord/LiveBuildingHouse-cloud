package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.CustomerInfo;

public interface CustomerInfoService extends IService<CustomerInfo> {

    Page<CustomerInfo> listPage(int pageNum, int pageSize, String customerName, String phone, String customerSource);

    CustomerInfo getOrCreateFromLive(String customerName, String phone, String remark);

    boolean addCustomer(CustomerInfo customer);

    boolean updateCustomer(CustomerInfo customer);

    boolean assignCustomer(Long id, Long consultantId);
}
