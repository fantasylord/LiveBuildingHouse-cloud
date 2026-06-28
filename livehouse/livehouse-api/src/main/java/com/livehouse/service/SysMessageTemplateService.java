package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.SysMessageTemplate;

import java.util.Map;

public interface SysMessageTemplateService extends IService<SysMessageTemplate> {

    Page<SysMessageTemplate> listPage(int pageNum, int pageSize, String keyword, Integer msgType, Integer status);

    SysMessageTemplate getActiveByCode(String templateCode);

    String render(String template, Map<String, Object> variables);
}
