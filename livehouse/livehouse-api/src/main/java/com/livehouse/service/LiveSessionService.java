package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.dto.LiveStartNotifyRequest;
import com.livehouse.dto.LiveStartNotifyResult;
import com.livehouse.entity.HouseReserve;
import com.livehouse.entity.LiveSession;

import java.util.Map;

/**
 * 直播场次服务接口
 */
public interface LiveSessionService extends IService<LiveSession> {

    /**
     * 分页查询直播场次列表
     */
    Page<LiveSession> listPage(int pageNum, int pageSize, String sessionName, Integer liveType, Integer status, Long platformId);

    /**
     * 获取直播场次详情
     */
    LiveSession getDetail(Long id);

    /**
     * 新增直播场次
     */
    boolean addSession(LiveSession session);

    /**
     * 更新直播场次
     */
    boolean updateSession(LiveSession session);

    /**
     * 删除直播场次
     */
    boolean deleteSession(Long id);

    /**
     * 开播
     */
    boolean startLive(Long id);

    /**
     * 关播
     */
    boolean stopLive(Long id);

    /**
     * 修改直播状态
     */
    boolean changeStatus(Long id, Integer status);

    boolean syncStatus(Long id);

    Map<String, Object> getPushInfo(Long id);

    Map<String, Object> getStatistics(Long id);

    Page<LiveSession> replayPage(int pageNum, int pageSize, String sessionName);

    boolean updateReplay(Long id, String replayUrl, Integer replayStatus);

    boolean deleteReplay(Long id);

    boolean handleStreamCallback(Map<String, Object> payload);

    boolean handleRecordCallback(Map<String, Object> payload);

    Page<LiveSession> h5List(int pageNum, int pageSize, String keyword, Integer status);

    Map<String, Object> h5Detail(Long id, boolean includePrivateUrl);

    Map<String, Object> checkAccess(Long id, String password, String phone);

    boolean recordView(Long id);

    HouseReserve reserveFromLive(Long id, Map<String, Object> payload);

    LiveStartNotifyResult notifyStart(Long id, LiveStartNotifyRequest request);
}
