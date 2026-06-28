package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.SmsRecord;
import com.livehouse.mapper.SmsRecordMapper;
import com.livehouse.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl extends ServiceImpl<SmsRecordMapper, SmsRecord> implements SmsService {

    @Override
    public Page<SmsRecord> listPage(int pageNum, int pageSize, String phone, Integer status) {
        LambdaQueryWrapper<SmsRecord> wrapper = new LambdaQueryWrapper<>();
        if (phone != null && !phone.isEmpty()) {
            wrapper.like(SmsRecord::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(SmsRecord::getStatus, status);
        }
        wrapper.orderByDesc(SmsRecord::getCreateTime);
        return baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean sendSms(String phone, String templateCode, String content) {
        SmsRecord record = new SmsRecord();
        record.setPhone(phone);
        record.setTemplateCode(templateCode);
        record.setContent(content);
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        save(record);
        try {
            record.setStatus(1);
            record.setSendTime(LocalDateTime.now());
            updateById(record);
            return true;
        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage());
            record.setStatus(2);
            record.setErrorMessage(e.getMessage());
            updateById(record);
            return false;
        }
    }

    @Override
    public boolean batchSendSms(String[] phones, String templateCode, String content) {
        for (String phone : phones) {
            sendSms(phone, templateCode, content);
        }
        return true;
    }
}