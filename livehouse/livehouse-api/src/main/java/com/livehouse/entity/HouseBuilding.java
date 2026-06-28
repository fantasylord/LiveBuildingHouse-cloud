package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 房源楼盘表 (house_building)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("house_building")
public class HouseBuilding extends BaseEntity {

    /**
     * 楼盘名称
     */
    private String buildingName;

    /**
     * 楼盘编码
     */
    private String buildingCode;

    private String province;

    private String city;

    private String district;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String coverImage;

    /**
     * 均价(元/㎡)
     */
    private BigDecimal avgPrice;

    /**
     * 最低价(元/㎡)
     */
    private BigDecimal minPrice;

    /**
     * 最高价(元/㎡)
     */
    private BigDecimal maxPrice;

    /**
     * 楼盘类型: 普通住宅/别墅/公寓/商业
     */
    private String buildingType;

    /**
     * 装修类型: 毛坯/简装/精装
     */
    private String decorationType;

    /**
     * 物业公司
     */
    private String propertyCompany;

    /**
     * 绿化率(%)
     */
    private BigDecimal greenRate;

    /**
     * 容积率
     */
    private BigDecimal plotRatio;

    /**
     * 总套数
     */
    private Integer totalUnits;

    /**
     * 楼盘介绍
     */
    private String description;

    /**
     * 图片集合(JSON数组)
     */
    private String images;

    private String developer;

    private Integer status;

    /**
     * 是否私密: 0-公开 1-私密
     */
    private Integer privated;

    private Integer sortOrder;

    private Integer viewCount;

    private String remark;
}