package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.CustomerFollow;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.mapper.CustomerFollowMapper;
import com.livehouse.mapper.CustomerInfoMapper;
import com.livehouse.service.CustomerFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerFollowServiceImpl extends ServiceImpl<CustomerFollowMapper, CustomerFollow> implements CustomerFollowService {

    @Autowired
    private CustomerInfoMapper customerInfoMapper;

    @Override
    public Page<CustomerFollow> listPage(int pageNum, int pageSize, Long customerId, String customerName) {
        LambdaQueryWrapper<CustomerFollow> wrapper = new LambdaQueryWrapper<CustomerFollow>()
                .eq(CustomerFollow::getDeleted, 0)
                .orderByDesc(CustomerFollow::getCreateTime);
        if (customerId != null) {
            wrapper.eq(CustomerFollow::getCustomerId, customerId);
        }
        if (StringUtils.hasText(customerName)) {
            List<CustomerInfo> customers = customerInfoMapper.selectList(new LambdaQueryWrapper<CustomerInfo>()
                    .like(CustomerInfo::getCustomerName, customerName)
                    .eq(CustomerInfo::getDeleted, 0));
            List<Long> ids = customers.stream().map(CustomerInfo::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            wrapper.in(CustomerFollow::getCustomerId, ids);
        }

        Page<CustomerFollow> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillCustomerInfo(page.getRecords());
        return page;
    }

    private void fillCustomerInfo(List<CustomerFollow> records) {
        List<Long> ids = records.stream()
                .map(CustomerFollow::getCustomerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, CustomerInfo> customerMap = customerInfoMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CustomerInfo::getId, customer -> customer));
        records.forEach(record -> {
            CustomerInfo customer = customerMap.get(record.getCustomerId());
            if (customer != null) {
                record.setCustomerName(customer.getCustomerName());
                record.setCustomerPhone(customer.getPhone());
            }
        });
    }
}
