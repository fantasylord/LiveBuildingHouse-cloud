package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.entity.HomeConfig;
import com.livehouse.mapper.HomeConfigMapper;
import com.livehouse.service.HomeConfigService;
import org.springframework.stereotype.Service;

@Service
public class HomeConfigServiceImpl extends ServiceImpl<HomeConfigMapper, HomeConfig> implements HomeConfigService {

    @Override
    public HomeConfig getByKey(String configKey) {
        return baseMapper.selectOne(new LambdaQueryWrapper<HomeConfig>()
                .eq(HomeConfig::getConfigKey, configKey)
                .eq(HomeConfig::getStatus, 1)
                .last("LIMIT 1"));
    }

    @Override
    public String getValueByKey(String configKey) {
        HomeConfig config = getByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
}