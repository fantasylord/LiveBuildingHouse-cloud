package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * H5 user setting.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_user_setting")
public class AppUserSetting extends BaseEntity {

    private Long userId;

    private Integer messageNotify;

    private Integer smsNotify;

    private Integer browseHistory;
}
