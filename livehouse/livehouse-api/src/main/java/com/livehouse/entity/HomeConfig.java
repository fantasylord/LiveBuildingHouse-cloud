package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("home_config")
public class HomeConfig extends BaseEntity {

    private String configKey;

    private String configName;

    private String configType;

    private String configValue;

    private Integer status;

    private Integer sortOrder;
}