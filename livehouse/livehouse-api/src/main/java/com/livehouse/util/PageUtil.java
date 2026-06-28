package com.livehouse.util;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.dto.PageRequest;
import com.livehouse.vo.PageResponse;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtil {

    /**
     * 构建MyBatis-Plus分页对象
     */
    public static <T> Page<T> buildPage(PageRequest pageRequest) {
        return new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    /**
     * 构建分页响应对象
     */
    public static <T> PageResponse<T> buildPageResponse(IPage<T> page) {
        return PageResponse.of(
            (int) page.getCurrent(),
            (int) page.getSize(),
            page.getTotal(),
            page.getRecords()
        );
    }

    /**
     * 构建查询条件（关键字搜索）
     */
    public static <T> QueryWrapper<T> buildKeywordQuery(QueryWrapper<T> queryWrapper, PageRequest pageRequest, String... fields) {
        if (StrUtil.isNotBlank(pageRequest.getKeyword())) {
            queryWrapper.and(wrapper -> {
                for (int i = 0; i < fields.length; i++) {
                    if (i == 0) {
                        wrapper.like(fields[i], pageRequest.getKeyword());
                    } else {
                        wrapper.or().like(fields[i], pageRequest.getKeyword());
                    }
                }
            });
        }
        return queryWrapper;
    }

    /**
     * 构建排序条件
     */
    public static <T> QueryWrapper<T> buildOrder(QueryWrapper<T> queryWrapper, PageRequest pageRequest) {
        if (StrUtil.isNotBlank(pageRequest.getSortField())) {
            if ("asc".equalsIgnoreCase(pageRequest.getSortOrder())) {
                queryWrapper.orderByAsc(pageRequest.getSortField());
            } else {
                queryWrapper.orderByDesc(pageRequest.getSortField());
            }
        }
        return queryWrapper;
    }

    /**
     * 构建完整查询条件（关键字搜索 + 排序）
     */
    public static <T> QueryWrapper<T> buildQuery(QueryWrapper<T> queryWrapper, PageRequest pageRequest, String... keywordFields) {
        return buildOrder(buildKeywordQuery(queryWrapper, pageRequest, keywordFields), pageRequest);
    }
}