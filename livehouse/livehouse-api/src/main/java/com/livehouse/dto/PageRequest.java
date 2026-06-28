package com.livehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求基类
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式: asc-升序 desc-降序
     */
    private String sortOrder = "asc";

    /**
     * 关键字搜索
     */
    private String keyword;

    /**
     * 获取分页起始位置
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}