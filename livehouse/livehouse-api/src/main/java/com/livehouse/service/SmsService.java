package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.SmsRecord;

public interface SmsService extends IService<SmsRecord> {

    Page<SmsRecord> listPage(int pageNum, int pageSize, String phone, Integer status);

    boolean sendSms(String phone, String templateCode, String content);

    boolean batchSendSms(String[] phones, String templateCode, String content);
}