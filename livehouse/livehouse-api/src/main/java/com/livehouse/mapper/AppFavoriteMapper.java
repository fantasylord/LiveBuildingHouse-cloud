package com.livehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.livehouse.entity.AppFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AppFavoriteMapper extends BaseMapper<AppFavorite> {

    @Select("SELECT * FROM app_favorite WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} LIMIT 1")
    AppFavorite selectAny(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    @Update("UPDATE app_favorite SET deleted = 0, target_title = #{targetTitle}, target_cover = #{targetCover}, target_desc = #{targetDesc}, update_time = NOW() WHERE id = #{id}")
    int restore(AppFavorite favorite);
}
