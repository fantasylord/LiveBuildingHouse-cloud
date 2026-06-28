package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.SysMessageTemplate;
import com.livehouse.mapper.SysMessageTemplateMapper;
import com.livehouse.service.SysMessageTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class SysMessageTemplateServiceImpl extends ServiceImpl<SysMessageTemplateMapper, SysMessageTemplate>
        implements SysMessageTemplateService {

    @Override
    public Page<SysMessageTemplate> listPage(int pageNum, int pageSize, String keyword, Integer msgType, Integer status) {
        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysMessageTemplate::getTemplateCode, keyword)
                    .or()
                    .like(SysMessageTemplate::getTemplateName, keyword));
        }
        if (msgType != null) {
            wrapper.eq(SysMessageTemplate::getMsgType, msgType);
        }
        if (status != null) {
            wrapper.eq(SysMessageTemplate::getStatus, status);
        }
        wrapper.orderByDesc(SysMessageTemplate::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public SysMessageTemplate getActiveByCode(String templateCode) {
        if (!StringUtils.hasText(templateCode)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<SysMessageTemplate>()
                .eq(SysMessageTemplate::getTemplateCode, templateCode)
                .eq(SysMessageTemplate::getStatus, 1)
                .last("LIMIT 1"));
    }

    @Override
    public String render(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template) || variables == null || variables.isEmpty()) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return rendered;
    }
}
