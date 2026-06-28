package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.LivePlatform;
import org.apache.ibatis.annotations.Mapper;

/**
 * 三方直播平台配置Mapper
 */
@Mapper
public interface LivePlatformMapper extends BaseMapper<LivePlatform> {

}