package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.common.exception.BusinessException;
import com.livehouse.entity.AppUser;
import com.livehouse.entity.LiveInteraction;
import com.livehouse.mapper.LiveInteractionMapper;
import com.livehouse.service.AppUserService;
import com.livehouse.service.LiveInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveInteractionServiceImpl extends ServiceImpl<LiveInteractionMapper, LiveInteraction> implements LiveInteractionService {

    private static final int TYPE_LIKE = 1;
    private static final int TYPE_DANMU = 2;
    private static final int STATUS_INACTIVE = 0;
    private static final int STATUS_ACTIVE = 1;

    private final AppUserService appUserService;

    @Override
    public Map<String, Object> getInteraction(Long sessionId, Long appUserId, String clientId, Long sinceId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        LambdaQueryWrapper<LiveInteraction> danmuWrapper = new LambdaQueryWrapper<LiveInteraction>()
                .eq(LiveInteraction::getSessionId, sessionId)
                .eq(LiveInteraction::getInteractionType, TYPE_DANMU)
                .eq(LiveInteraction::getStatus, STATUS_ACTIVE)
                .orderByAsc(LiveInteraction::getId)
                .last("LIMIT " + safeLimit);
        if (sinceId != null && sinceId > 0) {
            danmuWrapper.gt(LiveInteraction::getId, sinceId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("danmuList", list(danmuWrapper));
        data.put("likeCount", countLikes(sessionId));
        data.put("liked", findLike(sessionId, appUserId, clientId) != null);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Long sessionId, Long appUserId, String clientId, String nickname) {
        validateIdentity(appUserId, clientId);
        LiveInteraction like = findLikeIncludingInactive(sessionId, appUserId, clientId);
        boolean liked;
        if (like == null) {
            like = new LiveInteraction();
            like.setSessionId(sessionId);
            like.setAppUserId(appUserId);
            like.setClientId(clientId);
            like.setInteractionType(TYPE_LIKE);
            like.setNickname(resolveNickname(appUserId, nickname));
            like.setStatus(STATUS_ACTIVE);
            save(like);
            liked = true;
        } else {
            like.setStatus(STATUS_ACTIVE == (like.getStatus() == null ? 0 : like.getStatus()) ? STATUS_INACTIVE : STATUS_ACTIVE);
            like.setNickname(resolveNickname(appUserId, nickname));
            updateById(like);
            liked = like.getStatus() == STATUS_ACTIVE;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("liked", liked);
        data.put("likeCount", countLikes(sessionId));
        return data;
    }

    @Override
    public LiveInteraction sendDanmu(Long sessionId, Long appUserId, String clientId, String nickname, String content) {
        validateIdentity(appUserId, clientId);
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("弹幕内容不能为空");
        }
        String safeContent = content.trim();
        if (safeContent.length() > 80) {
            throw new BusinessException("弹幕内容不能超过80个字符");
        }

        LiveInteraction danmu = new LiveInteraction();
        danmu.setSessionId(sessionId);
        danmu.setAppUserId(appUserId);
        danmu.setClientId(clientId);
        danmu.setInteractionType(TYPE_DANMU);
        danmu.setContent(safeContent);
        danmu.setNickname(resolveNickname(appUserId, nickname));
        danmu.setStatus(STATUS_ACTIVE);
        save(danmu);
        return danmu;
    }

    private Long countLikes(Long sessionId) {
        return count(new LambdaQueryWrapper<LiveInteraction>()
                .eq(LiveInteraction::getSessionId, sessionId)
                .eq(LiveInteraction::getInteractionType, TYPE_LIKE)
                .eq(LiveInteraction::getStatus, STATUS_ACTIVE));
    }

    private LiveInteraction findLike(Long sessionId, Long appUserId, String clientId) {
        LiveInteraction like = findLikeIncludingInactive(sessionId, appUserId, clientId);
        return like != null && like.getStatus() != null && like.getStatus() == STATUS_ACTIVE ? like : null;
    }

    private LiveInteraction findLikeIncludingInactive(Long sessionId, Long appUserId, String clientId) {
        LambdaQueryWrapper<LiveInteraction> wrapper = new LambdaQueryWrapper<LiveInteraction>()
                .eq(LiveInteraction::getSessionId, sessionId)
                .eq(LiveInteraction::getInteractionType, TYPE_LIKE)
                .last("LIMIT 1");
        if (appUserId != null) {
            wrapper.eq(LiveInteraction::getAppUserId, appUserId);
        } else {
            wrapper.eq(LiveInteraction::getClientId, clientId);
        }
        return getOne(wrapper);
    }

    private void validateIdentity(Long appUserId, String clientId) {
        if (appUserId == null && !StringUtils.hasText(clientId)) {
            throw new BusinessException("缺少互动身份标识");
        }
    }

    private String resolveNickname(Long appUserId, String nickname) {
        if (appUserId != null) {
            AppUser user = appUserService.getById(appUserId);
            if (user != null) {
                if (StringUtils.hasText(user.getRealName())) {
                    return user.getRealName();
                }
                if (StringUtils.hasText(user.getUsername())) {
                    return user.getUsername();
                }
            }
        }
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        return "游客";
    }
}
