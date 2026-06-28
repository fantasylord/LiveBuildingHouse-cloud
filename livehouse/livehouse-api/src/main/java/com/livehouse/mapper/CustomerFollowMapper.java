package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.CustomerFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户跟进记录Mapper
 */
@Mapper
public interface CustomerFollowMapper extends BaseMapper<CustomerFollow> {
}