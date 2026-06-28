package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.LivePlatform;
import com.livehouse.mapper.LivePlatformMapper;
import com.livehouse.service.LivePlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 三方直播平台配置服务实现类
 */
@Slf4j
@Service
public class LivePlatformServiceImpl extends ServiceImpl<LivePlatformMapper, LivePlatform> 
        implements LivePlatformService {

    @Override
    public Page<LivePlatform> listPage(int pageNum, int pageSize, String platformName, String platformType, Integer status) {
        Page<LivePlatform> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<LivePlatform> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(platformName)) {
            wrapper.like(LivePlatform::getPlatformName, platformName);
        }
        if (StringUtils.hasText(platformType)) {
            wrapper.eq(LivePlatform::getPlatformType, platformType);
        }
        if (status != null) {
            wrapper.eq(LivePlatform::getStatus, status);
        }
        
        wrapper.eq(LivePlatform::getDeleted, 0)
               .orderByAsc(LivePlatform::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public LivePlatform getDetail(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public List<LivePlatform> getList(Integer status) {
        LambdaQueryWrapper<LivePlatform> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(LivePlatform::getStatus, status);
        }
        
        wrapper.eq(LivePlatform::getDeleted, 0)
               .orderByAsc(LivePlatform::getCreateTime);
        
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public boolean addPlatform(LivePlatform platform) {
        if (platform == null) {
            return false;
        }
        
        if (platform.getExpireTime() == null) {
            platform.setExpireTime(3600);
        }
        
        if (platform.getStatus() == null) {
            platform.setStatus(1);
        }
        
        return this.save(platform);
    }

    @Override
    public boolean updatePlatform(LivePlatform platform) {
        if (platform == null || platform.getId() == null) {
            return false;
        }
        return this.updateById(platform);
    }

    @Override
    public boolean deletePlatform(Long id) {
        return this.removeById(id);
    }
}