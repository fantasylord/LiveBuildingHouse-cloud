package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.LiveSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 直播场次Mapper
 */
@Mapper
public interface LiveSessionMapper extends BaseMapper<LiveSession> {
}