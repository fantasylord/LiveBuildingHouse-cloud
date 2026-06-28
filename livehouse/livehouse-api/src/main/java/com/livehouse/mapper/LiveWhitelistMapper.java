package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.LiveWhitelist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 直播白名单Mapper
 */
@Mapper
public interface LiveWhitelistMapper extends BaseMapper<LiveWhitelist> {
}