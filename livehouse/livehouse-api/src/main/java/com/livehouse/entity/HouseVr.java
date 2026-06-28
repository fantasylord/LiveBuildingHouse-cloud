package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * VR素材表 (house_vr)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("house_vr")
public class HouseVr extends BaseEntity {

    private Long buildingId;

    private Long unitId;

    private String vrName;

    private String vrCode;

    private String panoramaUrl;

    private String thumbnailUrl;


    private String scenes;

    private String narrationText;

    private String narrationAudio;

    private Integer privated;

    private Integer accessCount;

    private Integer sortOrder;

    private Integer status;

    private String remark;

    /**
     * 楼盘名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String buildingName;

    /**
     * 户型名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String unitName;
}