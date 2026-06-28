package com.livehouse.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应基类
 */
@Data
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    public PageResponse() {
    }

    public PageResponse(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < totalPages;
    }

    /**
     * 构建分页响应
     */
    public static <T> PageResponse<T> of(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        return new PageResponse<>(pageNum, pageSize, total, records);
    }
}