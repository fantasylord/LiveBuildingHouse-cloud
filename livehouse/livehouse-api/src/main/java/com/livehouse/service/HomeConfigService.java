package com.livehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.HomeConfig;

public interface HomeConfigService extends IService<HomeConfig> {

    HomeConfig getByKey(String configKey);

    String getValueByKey(String configKey);
}