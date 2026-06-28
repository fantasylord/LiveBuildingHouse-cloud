package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * H5 browse record.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_browse_record")
public class AppBrowseRecord extends BaseEntity {

    private Long userId;

    private String targetType;

    private Long targetId;

    private String targetTitle;

    private String targetCover;

    private String targetDesc;
}
