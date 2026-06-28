package com.livehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.LiveInteraction;

import java.util.Map;

public interface LiveInteractionService extends IService<LiveInteraction> {

    Map<String, Object> getInteraction(Long sessionId, Long appUserId, String clientId, Long sinceId, int limit);

    Map<String, Object> toggleLike(Long sessionId, Long appUserId, String clientId, String nickname);

    LiveInteraction sendDanmu(Long sessionId, Long appUserId, String clientId, String nickname, String content);
}
