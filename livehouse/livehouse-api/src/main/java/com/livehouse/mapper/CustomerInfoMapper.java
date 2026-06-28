package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.CustomerInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户信息Mapper
 */
@Mapper
public interface CustomerInfoMapper extends BaseMapper<CustomerInfo> {
}