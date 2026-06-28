package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * H5 favorite record.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_favorite")
public class AppFavorite extends BaseEntity {

    private Long userId;

    private String targetType;

    private Long targetId;

    private String targetTitle;

    private String targetCover;

    private String targetDesc;
}
