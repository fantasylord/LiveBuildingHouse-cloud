package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.LiveWhitelist;
import com.livehouse.entity.LiveSession;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.mapper.LiveSessionMapper;
import com.livehouse.mapper.LiveWhitelistMapper;
import com.livehouse.service.CustomerInfoService;
import com.livehouse.service.LiveWhitelistService;
import com.livehouse.vo.WhitelistBatchImportResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;
import java.util.List;

/**
 * 直播白名单服务实现类
 */
@Slf4j
@Service
public class LiveWhitelistServiceImpl extends ServiceImpl<LiveWhitelistMapper, LiveWhitelist> 
        implements LiveWhitelistService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Autowired
    private LiveSessionMapper liveSessionMapper;

    @Autowired
    private CustomerInfoService customerInfoService;

    @Override
    public Page<LiveWhitelist> listPage(int pageNum, int pageSize, Long sessionId, String phone) {
        Page<LiveWhitelist> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<LiveWhitelist> wrapper = new LambdaQueryWrapper<>();
        
        if (sessionId != null) {
            wrapper.eq(LiveWhitelist::getSessionId, sessionId);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(LiveWhitelist::getPhone, phone);
        }
        
        wrapper.eq(LiveWhitelist::getDeleted, 0)
               .orderByDesc(LiveWhitelist::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public LiveWhitelist getDetail(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public boolean addWhitelist(LiveWhitelist whitelist) {
        if (whitelist == null || whitelist.getSessionId() == null || !StringUtils.hasText(whitelist.getPhone())) {
            return false;
        }

        whitelist.setPhone(whitelist.getPhone().trim());
        if (!PHONE_PATTERN.matcher(whitelist.getPhone()).matches()) {
            log.warn("手机号格式错误: {}", whitelist.getPhone());
            return false;
        }

        LiveSession session = liveSessionMapper.selectById(whitelist.getSessionId());
        if (session == null || (session.getDeleted() != null && session.getDeleted() == 1)) {
            log.warn("直播场次不存在，sessionId: {}", whitelist.getSessionId());
            return false;
        }
        
        // 检查是否已存在
        LambdaQueryWrapper<LiveWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveWhitelist::getSessionId, whitelist.getSessionId())
               .eq(LiveWhitelist::getPhone, whitelist.getPhone())
               .eq(LiveWhitelist::getDeleted, 0);
        
        long count = this.count(wrapper);
        if (count > 0) {
            log.warn("该手机号已在白名单中");
            return false;
        }
        
        boolean saved = this.save(whitelist);
        if (saved) {
            customerInfoService.getOrCreateFromLive(whitelist.getCustomerName(), whitelist.getPhone(), "直播白名单：" + session.getSessionName());
        }
        return saved;
    }

    @Override
    public WhitelistBatchImportResult batchImport(Long sessionId, String content) {
        WhitelistBatchImportResult result = new WhitelistBatchImportResult();
        if (sessionId == null || !StringUtils.hasText(content)) {
            result.getErrors().add("场次ID和导入内容不能为空");
            return result;
        }

        LiveSession session = liveSessionMapper.selectById(sessionId);
        if (session == null || (session.getDeleted() != null && session.getDeleted() == 1)) {
            result.getErrors().add("直播场次不存在");
            return result;
        }
        
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i].trim();
            if (!StringUtils.hasText(line)) {
                continue;
            }
            result.setTotal(result.getTotal() + 1);
            ParsedWhitelist parsed = parseLine(line);
            if (!PHONE_PATTERN.matcher(parsed.phone).matches()) {
                result.setInvalid(result.getInvalid() + 1);
                result.getErrors().add("第" + (i + 1) + "行手机号无效: " + line);
                continue;
            }
            if (exists(sessionId, parsed.phone)) {
                result.setDuplicate(result.getDuplicate() + 1);
                result.getErrors().add("第" + (i + 1) + "行手机号已存在: " + parsed.phone);
                continue;
            }

            LiveWhitelist whitelist = new LiveWhitelist();
            whitelist.setSessionId(sessionId);
            whitelist.setPhone(parsed.phone);
            whitelist.setCustomerName(parsed.customerName);
            whitelist.setRemark(parsed.remark);
            if (this.save(whitelist)) {
                result.setSuccess(result.getSuccess() + 1);
                customerInfoService.getOrCreateFromLive(parsed.customerName, parsed.phone, "直播白名单：" + session.getSessionName());
            } else {
                result.setInvalid(result.getInvalid() + 1);
                result.getErrors().add("第" + (i + 1) + "行保存失败: " + line);
            }
        }
        
        log.info("批量导入白名单完成，总数: {}, 成功: {}, 重复: {}, 无效: {}",
                result.getTotal(), result.getSuccess(), result.getDuplicate(), result.getInvalid());
        return result;
    }

    @Override
    public WhitelistBatchImportResult batchImportCustomers(Long sessionId, List<Long> customerIds, String remark) {
        WhitelistBatchImportResult result = new WhitelistBatchImportResult();
        if (sessionId == null || customerIds == null || customerIds.isEmpty()) {
            result.getErrors().add("场次和客户不能为空");
            return result;
        }
        LiveSession session = liveSessionMapper.selectById(sessionId);
        if (session == null || (session.getDeleted() != null && session.getDeleted() == 1)) {
            result.getErrors().add("直播场次不存在");
            return result;
        }
        for (Long customerId : customerIds) {
            result.setTotal(result.getTotal() + 1);
            CustomerInfo customer = customerInfoService.getById(customerId);
            if (customer == null || customer.getDeleted() != null && customer.getDeleted() == 1 || !StringUtils.hasText(customer.getPhone())) {
                result.setInvalid(result.getInvalid() + 1);
                result.getErrors().add("客户不存在或手机号为空: " + customerId);
                continue;
            }
            String phone = customer.getPhone().trim();
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                result.setInvalid(result.getInvalid() + 1);
                result.getErrors().add("客户手机号无效: " + customer.getCustomerName() + " " + phone);
                continue;
            }
            if (exists(sessionId, phone)) {
                result.setDuplicate(result.getDuplicate() + 1);
                result.getErrors().add("手机号已存在: " + customer.getCustomerName() + " " + phone);
                continue;
            }
            LiveWhitelist whitelist = new LiveWhitelist();
            whitelist.setSessionId(sessionId);
            whitelist.setPhone(phone);
            whitelist.setCustomerName(customer.getCustomerName());
            whitelist.setRemark(remark);
            if (this.save(whitelist)) {
                result.setSuccess(result.getSuccess() + 1);
            } else {
                result.setInvalid(result.getInvalid() + 1);
                result.getErrors().add("保存失败: " + customer.getCustomerName() + " " + phone);
            }
        }
        return result;
    }

    @Override
    public boolean deleteWhitelist(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean isInWhitelist(Long sessionId, String phone) {
        if (sessionId == null || !StringUtils.hasText(phone)) {
            return false;
        }
        
        LambdaQueryWrapper<LiveWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveWhitelist::getSessionId, sessionId)
               .eq(LiveWhitelist::getPhone, phone)
               .eq(LiveWhitelist::getDeleted, 0);
        
        return this.count(wrapper) > 0;
    }

    private boolean exists(Long sessionId, String phone) {
        LambdaQueryWrapper<LiveWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveWhitelist::getSessionId, sessionId)
                .eq(LiveWhitelist::getPhone, phone)
                .eq(LiveWhitelist::getDeleted, 0);
        return this.count(wrapper) > 0;
    }

    private ParsedWhitelist parseLine(String line) {
        String[] parts = line.contains(",")
                ? line.split(",", 3)
                : line.split("\\s+", 3);
        ParsedWhitelist parsed = new ParsedWhitelist();
        parsed.phone = parts.length > 0 ? parts[0].trim() : "";
        parsed.customerName = parts.length > 1 ? parts[1].trim() : "";
        parsed.remark = parts.length > 2 ? parts[2].trim() : "";
        return parsed;
    }

    private static class ParsedWhitelist {
        private String phone;
        private String customerName;
        private String remark;
    }
}
