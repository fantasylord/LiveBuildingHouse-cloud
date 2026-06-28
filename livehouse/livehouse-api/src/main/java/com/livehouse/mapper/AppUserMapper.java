package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
