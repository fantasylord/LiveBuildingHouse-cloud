package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.mapper.CustomerInfoMapper;
import com.livehouse.service.CustomerInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {

    @Override
    public Page<CustomerInfo> listPage(int pageNum, int pageSize, String customerName, String phone, String customerSource) {
        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<CustomerInfo>()
                .eq(CustomerInfo::getDeleted, 0)
                .orderByDesc(CustomerInfo::getCreateTime);
        if (StringUtils.hasText(customerName)) {
            wrapper.like(CustomerInfo::getCustomerName, customerName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(CustomerInfo::getPhone, phone);
        }
        if (StringUtils.hasText(customerSource)) {
            wrapper.eq(CustomerInfo::getCustomerSource, customerSource);
        }
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean addCustomer(CustomerInfo customer) {
        if (customer == null || !StringUtils.hasText(customer.getCustomerName()) || !StringUtils.hasText(customer.getPhone())) {
            return false;
        }
        CustomerInfo exists = this.getOne(new LambdaQueryWrapper<CustomerInfo>()
                .eq(CustomerInfo::getPhone, customer.getPhone())
                .eq(CustomerInfo::getDeleted, 0)
                .last("LIMIT 1"));
        if (exists != null) {
            return false;
        }
        if (!StringUtils.hasText(customer.getCustomerCode())) {
            customer.setCustomerCode("CUS" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(customer.getCustomerSource())) {
            customer.setCustomerSource("3");
        }
        if (customer.getCustomerLevel() == null) {
            customer.setCustomerLevel(1);
        }
        if (customer.getIntentionStatus() == null) {
            customer.setIntentionStatus(1);
        }
        if (customer.getFollowCount() == null) {
            customer.setFollowCount(0);
        }
        return this.save(customer);
    }

    @Override
    public boolean updateCustomer(CustomerInfo customer) {
        if (customer == null || customer.getId() == null) {
            return false;
        }
        return this.updateById(customer);
    }

    @Override
    public boolean assignCustomer(Long id, Long consultantId) {
        if (id == null || consultantId == null) {
            return false;
        }
        CustomerInfo customer = new CustomerInfo();
        customer.setId(id);
        customer.setAssignConsultantId(consultantId);
        customer.setAssignTime(LocalDateTime.now());
        return this.updateById(customer);
    }

    @Override
    public CustomerInfo getOrCreateFromLive(String customerName, String phone, String remark) {
        CustomerInfo customer = this.getOne(new LambdaQueryWrapper<CustomerInfo>()
                .eq(CustomerInfo::getPhone, phone)
                .eq(CustomerInfo::getDeleted, 0)
                .last("LIMIT 1"));
        if (customer != null) {
            if (StringUtils.hasText(customerName)) {
                customer.setCustomerName(customerName);
            }
            customer.setCustomerSource("2");
            customer.setLastFollowTime(LocalDateTime.now());
            customer.setFollowCount((customer.getFollowCount() == null ? 0 : customer.getFollowCount()) + 1);
            if (StringUtils.hasText(remark)) {
                customer.setRemark(remark);
            }
            this.updateById(customer);
            return customer;
        }

        customer = new CustomerInfo();
        customer.setCustomerName(StringUtils.hasText(customerName) ? customerName : "直播客户");
        customer.setPhone(phone);
        customer.setCustomerCode("CUS" + System.currentTimeMillis());
        customer.setCustomerSource("2");
        customer.setCustomerLevel(1);
        customer.setIntentionStatus(1);
        customer.setFollowCount(0);
        customer.setFirstVisitTime(LocalDateTime.now());
        customer.setLastFollowTime(LocalDateTime.now());
        customer.setRemark(remark);
        this.save(customer);
        return customer;
    }
}
