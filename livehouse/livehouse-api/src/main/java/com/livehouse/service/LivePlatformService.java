package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.LivePlatform;

import java.util.List;

/**
 * 三方直播平台配置服务接口
 */
public interface LivePlatformService extends IService<LivePlatform> {

    /**
     * 分页查询直播平台列表
     */
    Page<LivePlatform> listPage(int pageNum, int pageSize, String platformName, String platformType, Integer status);

    /**
     * 获取直播平台详情
     */
    LivePlatform getDetail(Long id);

    /**
     * 获取直播平台列表（不分页）
     */
    List<LivePlatform> getList(Integer status);

    /**
     * 新增直播平台
     */
    boolean addPlatform(LivePlatform platform);

    /**
     * 更新直播平台
     */
    boolean updatePlatform(LivePlatform platform);

    /**
     * 删除直播平台
     */
    boolean deletePlatform(Long id);
}