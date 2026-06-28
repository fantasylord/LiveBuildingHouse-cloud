package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 户型表 (house_unit)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("house_unit")
public class HouseUnit extends BaseEntity {

    /**
     * 楼盘ID
     */
    private Long buildingId;
    
    /**
     * 户型名称
     */
    private String unitName;

    /**
     * 户型编码
     */
    private String unitCode;

    /**
     * 室
     */
    private Integer rooms;

    /**
     * 厅
     */
    private Integer halls;

    /**
     * 卫
     */
    private Integer bathrooms;

    /**
     * 面积(㎡)
     */
    private BigDecimal area;

    /**
     * 总价(万元)
     */
    private BigDecimal price;

    /**
     * 朝向
     */
    private String orientation;

    /**
     * 楼层范围
     */
    private String floorRange;

    /**
     * 总楼层
     */
    private Integer totalFloor;

    /**
     * 户型介绍
     */
    private String description;

    /**
     * 户型图片JSON数组
     */
    private String images;

    /**
     * 状态: 0-下架 1-上架
     */
    private Integer status;
    
    /**
     * 所属楼盘名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String buildingName;
}