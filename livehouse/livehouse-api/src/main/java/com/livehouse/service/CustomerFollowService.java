package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.CustomerFollow;

public interface CustomerFollowService extends IService<CustomerFollow> {

    Page<CustomerFollow> listPage(int pageNum, int pageSize, Long customerId, String customerName);
}
