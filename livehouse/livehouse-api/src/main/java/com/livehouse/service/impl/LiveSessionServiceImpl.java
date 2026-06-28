package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.common.constants.MessageConstants;
import com.livehouse.dto.LiveStartNotifyRequest;
import com.livehouse.dto.LiveStartNotifyResult;
import com.livehouse.dto.MessageSendRequest;
import com.livehouse.dto.MessageSendResult;
import com.livehouse.entity.AppUser;
import com.livehouse.entity.CustomerFollow;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.entity.HouseReserve;
import com.livehouse.entity.LiveAddress;
import com.livehouse.entity.LivePlatform;
import com.livehouse.entity.LiveSession;
import com.livehouse.entity.SysMessage;
import com.livehouse.entity.SysMessageTemplate;
import com.livehouse.mapper.LivePlatformMapper;
import com.livehouse.mapper.LiveSessionMapper;
import com.livehouse.service.AppUserService;
import com.livehouse.service.CustomerFollowService;
import com.livehouse.service.CustomerInfoService;
import com.livehouse.service.HouseReserveService;
import com.livehouse.service.LiveAddressService;
import com.livehouse.service.LiveSessionService;
import com.livehouse.service.LiveWhitelistService;
import com.livehouse.service.SmsService;
import com.livehouse.service.SysMessageService;
import com.livehouse.service.SysMessageTemplateService;
import com.livehouse.service.TencentLiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 直播场次服务实现类
 */
@Slf4j
@Service
public class LiveSessionServiceImpl extends ServiceImpl<LiveSessionMapper, LiveSession> 
        implements LiveSessionService {

    @Autowired
    private LivePlatformMapper livePlatformMapper;

    @Autowired
    private LiveAddressService liveAddressService;

    @Autowired
    private TencentLiveService tencentLiveService;

    @Autowired
    private LiveWhitelistService liveWhitelistService;

    @Autowired
    private CustomerInfoService customerInfoService;

    @Autowired
    private HouseReserveService houseReserveService;

    @Autowired
    private CustomerFollowService customerFollowService;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private SysMessageService sysMessageService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SysMessageTemplateService messageTemplateService;

    @Override
    public Page<LiveSession> listPage(int pageNum, int pageSize, String sessionName, Integer liveType, Integer status, Long platformId) {
        Page<LiveSession> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<LiveSession> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(sessionName)) {
            wrapper.like(LiveSession::getSessionName, sessionName);
        }
        if (liveType != null) {
            wrapper.eq(LiveSession::getLiveType, liveType);
        }
        if (status != null) {
            wrapper.eq(LiveSession::getStatus, status);
        }
        if (platformId != null) {
            wrapper.eq(LiveSession::getPlatformId, platformId);
        }
        
        wrapper.eq(LiveSession::getDeleted, 0)
               .orderByDesc(LiveSession::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public LiveSession getDetail(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public boolean addSession(LiveSession session) {
        if (session == null) {
            return false;
        }
        
        if (!StringUtils.hasText(session.getSessionCode())) {
            session.setSessionCode("LIVE" + System.currentTimeMillis());
        }
        
        if (session.getStatus() == null) {
            session.setStatus(0);
        }
        if (session.getReplayStatus() == null) {
            session.setReplayStatus(0);
        }
        
        if (session.getPlatformId() != null) {
            LivePlatform platform = livePlatformMapper.selectById(session.getPlatformId());
            if (platform != null) {
                LiveAddress address = liveAddressService.generateLiveAddress(platform, null, null);
                if (address != null) {
                    session.setStreamId(address.getStreamId());
                    session.setPushUrl(address.getPushUrl());
                    session.setPlayUrl(address.getPlayUrl());
                    log.info("为直播场次 {} 生成直播地址，平台: {}", session.getSessionName(), platform.getPlatformName());
                }
            }
        }
        
        if (!StringUtils.hasText(session.getPlayUrl())) {
            String streamId = StringUtils.hasText(session.getStreamId()) ? session.getStreamId() : 
                "stream_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            session.setStreamId(streamId);
            session.setPushUrl("rtmp://push.livehouse.cn/live/" + streamId);
            session.setPlayUrl("https://play.livehouse.cn/live/" + streamId + ".m3u8");
            log.info("为直播场次 {} 生成默认直播地址（未配置平台）", session.getSessionName());
        }
        
        return this.save(session);
    }

    @Override
    public boolean updateSession(LiveSession session) {
        if (session == null || session.getId() == null) {
            return false;
        }
        return this.updateById(session);
    }

    @Override
    public boolean deleteSession(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean startLive(Long id) {
        if (id == null) {
            return false;
        }
        
        LiveSession session = this.getById(id);
        if (session == null) {
            log.warn("直播场次不存在，id: {}", id);
            return false;
        }
        
        session.setStatus(1);
        session.setActualStartTime(LocalDateTime.now());
        
        return this.updateById(session);
    }

    @Override
    public boolean stopLive(Long id) {
        if (id == null) {
            return false;
        }
        
        LiveSession session = this.getById(id);
        if (session == null) {
            log.warn("直播场次不存在，id: {}", id);
            return false;
        }
        
        session.setStatus(2);
        session.setActualEndTime(LocalDateTime.now());
        if (session.getReplayStatus() == null || session.getReplayStatus() == 0) {
            session.setReplayStatus(1);
        }
        
        return this.updateById(session);
    }

    @Override
    public boolean changeStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        
        LiveSession session = new LiveSession();
        session.setId(id);
        session.setStatus(status);
        
        return this.updateById(session);
    }

    @Override
    public boolean syncStatus(Long id) {
        LiveSession session = this.getById(id);
        if (session == null) {
            return false;
        }
        Integer status = tencentLiveService.queryStreamStatus(session);
        if (status == null) {
            return true;
        }
        session.setStatus(status);
        if (status == 1 && session.getActualStartTime() == null) {
            session.setActualStartTime(LocalDateTime.now());
        }
        if (status == 2 && session.getActualEndTime() == null) {
            session.setActualEndTime(LocalDateTime.now());
        }
        return this.updateById(session);
    }

    @Override
    public Map<String, Object> getPushInfo(Long id) {
        LiveSession session = this.getById(id);
        Map<String, Object> data = new HashMap<>();
        if (session == null) {
            return data;
        }
        data.put("id", session.getId());
        data.put("sessionName", session.getSessionName());
        data.put("streamId", session.getStreamId());
        data.put("pushUrl", session.getPushUrl());
        data.put("playUrl", session.getPlayUrl());
        data.put("status", session.getStatus());
        data.put("maxViewer", session.getMaxViewer() == null ? 0 : session.getMaxViewer());
        data.put("totalViewer", session.getTotalViewer() == null ? 0 : session.getTotalViewer());
        data.put("reserveCount", session.getReserveCount() == null ? 0 : session.getReserveCount());
        data.put("leaveCount", session.getLeaveCount() == null ? 0 : session.getLeaveCount());
        return data;
    }

    @Override
    public Map<String, Object> getStatistics(Long id) {
        LiveSession session = this.getById(id);
        Map<String, Object> data = new HashMap<>();
        if (session == null) {
            return data;
        }
        data.put("maxViewer", session.getMaxViewer() == null ? 0 : session.getMaxViewer());
        data.put("totalViewer", session.getTotalViewer() == null ? 0 : session.getTotalViewer());
        data.put("reserveCount", session.getReserveCount() == null ? 0 : session.getReserveCount());
        data.put("leaveCount", session.getLeaveCount() == null ? 0 : session.getLeaveCount());
        data.put("replayStatus", session.getReplayStatus() == null ? 0 : session.getReplayStatus());
        data.put("replayUrl", session.getReplayUrl());
        return data;
    }

    @Override
    public Page<LiveSession> replayPage(int pageNum, int pageSize, String sessionName) {
        LambdaQueryWrapper<LiveSession> wrapper = new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .isNotNull(LiveSession::getReplayUrl)
                .ne(LiveSession::getReplayUrl, "")
                .orderByDesc(LiveSession::getActualEndTime);
        if (StringUtils.hasText(sessionName)) {
            wrapper.like(LiveSession::getSessionName, sessionName);
        }
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public boolean updateReplay(Long id, String replayUrl, Integer replayStatus) {
        LiveSession session = new LiveSession();
        session.setId(id);
        session.setReplayUrl(replayUrl);
        session.setReplayStatus(replayStatus == null ? 2 : replayStatus);
        return this.updateById(session);
    }

    @Override
    public boolean deleteReplay(Long id) {
        LiveSession session = new LiveSession();
        session.setId(id);
        session.setReplayUrl("");
        session.setReplayStatus(0);
        return this.updateById(session);
    }

    @Override
    public boolean handleStreamCallback(Map<String, Object> payload) {
        String streamId = readString(payload, "stream_id", "streamId", "stream_name", "streamName");
        if (!StringUtils.hasText(streamId)) {
            return false;
        }
        LiveSession session = getByStreamId(streamId);
        if (session == null) {
            return false;
        }
        Integer eventType = readInteger(payload, "event_type", "eventType");
        Integer status = readInteger(payload, "status", "streamStatus");
        if (eventType != null) {
            status = eventType == 1 ? 1 : 2;
        }
        if (status == null) {
            status = 1;
        }
        session.setStatus(status);
        if (status == 1 && session.getActualStartTime() == null) {
            session.setActualStartTime(LocalDateTime.now());
        }
        if (status == 2 && session.getActualEndTime() == null) {
            session.setActualEndTime(LocalDateTime.now());
        }
        Integer totalViewer = readInteger(payload, "total_viewer", "totalViewer", "viewerCount");
        Integer maxViewer = readInteger(payload, "max_viewer", "maxViewer", "onlinePeak");
        if (totalViewer != null) {
            session.setTotalViewer(totalViewer);
        }
        if (maxViewer != null) {
            session.setMaxViewer(maxViewer);
        }
        return this.updateById(session);
    }

    @Override
    public boolean handleRecordCallback(Map<String, Object> payload) {
        String streamId = readString(payload, "stream_id", "streamId", "stream_name", "streamName");
        String replayUrl = readString(payload, "video_url", "videoUrl", "file_url", "fileUrl", "replayUrl");
        if (!StringUtils.hasText(streamId) || !StringUtils.hasText(replayUrl)) {
            return false;
        }
        LiveSession session = getByStreamId(streamId);
        if (session == null) {
            return false;
        }
        session.setReplayUrl(replayUrl);
        session.setReplayStatus(2);
        if (session.getStatus() == null || session.getStatus() == 1) {
            session.setStatus(2);
        }
        if (session.getActualEndTime() == null) {
            session.setActualEndTime(LocalDateTime.now());
        }
        return this.updateById(session);
    }

    @Override
    public Page<LiveSession> h5List(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<LiveSession> wrapper = new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .in(LiveSession::getStatus, 0, 1, 2)
                .orderByAsc(LiveSession::getStatus)
                .orderByDesc(LiveSession::getStartTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(LiveSession::getSessionName, keyword);
        }
        if (status != null) {
            wrapper.eq(LiveSession::getStatus, status);
        }
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Map<String, Object> h5Detail(Long id, boolean includePrivateUrl) {
        LiveSession session = this.getById(id);
        Map<String, Object> data = new HashMap<>();
        if (session == null) {
            return data;
        }
        data.put("id", session.getId());
        data.put("sessionName", session.getSessionName());
        data.put("buildingId", session.getBuildingId());
        data.put("unitId", session.getUnitId());
        data.put("liveType", session.getLiveType());
        data.put("status", session.getStatus());
        data.put("anchorName", session.getAnchorName());
        data.put("startTime", session.getStartTime());
        data.put("endTime", session.getEndTime());
        data.put("introduction", session.getIntroduction());
        data.put("totalViewer", session.getTotalViewer() == null ? 0 : session.getTotalViewer());
        data.put("maxViewer", session.getMaxViewer() == null ? 0 : session.getMaxViewer());
        data.put("reserveCount", session.getReserveCount() == null ? 0 : session.getReserveCount());
        data.put("leaveCount", session.getLeaveCount() == null ? 0 : session.getLeaveCount());
        boolean publicLive = session.getLiveType() == null || session.getLiveType() == 1;
        if (publicLive || includePrivateUrl) {
            data.put("playUrl", session.getPlayUrl());
            data.put("replayUrl", session.getReplayUrl());
        }
        data.put("needAccess", !publicLive && !includePrivateUrl);
        return data;
    }

    @Override
    public Map<String, Object> checkAccess(Long id, String password, String phone) {
        LiveSession session = this.getById(id);
        Map<String, Object> data = new HashMap<>();
        if (session == null) {
            data.put("granted", false);
            data.put("message", "直播不存在");
            return data;
        }
        boolean granted = session.getLiveType() == null || session.getLiveType() == 1;
        if (!granted && StringUtils.hasText(session.getPassword())) {
            granted = session.getPassword().equals(password);
        }
        if (!granted && StringUtils.hasText(phone)) {
            granted = liveWhitelistService.isInWhitelist(id, phone);
        }
        data.put("granted", granted);
        data.put("message", granted ? "校验通过" : "无权限观看该直播");
        if (granted) {
            data.putAll(h5Detail(id, true));
        }
        return data;
    }

    @Override
    public boolean recordView(Long id) {
        LiveSession session = this.getById(id);
        if (session == null) {
            return false;
        }
        session.setTotalViewer((session.getTotalViewer() == null ? 0 : session.getTotalViewer()) + 1);
        int currentMax = session.getMaxViewer() == null ? 0 : session.getMaxViewer();
        session.setMaxViewer(Math.max(currentMax, session.getTotalViewer()));
        return this.updateById(session);
    }

    @Override
    public HouseReserve reserveFromLive(Long id, Map<String, Object> payload) {
        LiveSession session = this.getById(id);
        if (session == null) {
            return null;
        }
        String customerName = readString(payload, "customerName", "name");
        String phone = readString(payload, "phone");
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        String remark = readString(payload, "remark");
        CustomerInfo customer = customerInfoService.getOrCreateFromLive(customerName, phone, remark);

        HouseReserve reserve = new HouseReserve();
        reserve.setAppUserId(readLong(payload, "appUserId"));
        reserve.setReserveCode("RSV" + System.currentTimeMillis());
        reserve.setCustomerName(StringUtils.hasText(customerName) ? customerName : customer.getCustomerName());
        reserve.setPhone(phone);
        Long buildingId = readLong(payload, "buildingId") == null ? session.getBuildingId() : readLong(payload, "buildingId");
        Long unitId = readLong(payload, "unitId") == null ? session.getUnitId() : readLong(payload, "unitId");
        reserve.setBuildingId(buildingId == null ? 0L : buildingId);
        reserve.setUnitId(unitId);
        reserve.setLiveSessionId(id);
        reserve.setVisitType(1);
        reserve.setVisitStatus(0);
        reserve.setRemark(remark);
        houseReserveService.save(reserve);

        CustomerFollow follow = new CustomerFollow();
        follow.setCustomerId(customer.getId());
        follow.setFollowType(0);
        follow.setFollowContent("直播留资：" + session.getSessionName());
        follow.setIntentionLevel(1);
        follow.setNextPlan("联系客户确认看房预约");
        customerFollowService.save(follow);

        session.setReserveCount((session.getReserveCount() == null ? 0 : session.getReserveCount()) + 1);
        session.setLeaveCount((session.getLeaveCount() == null ? 0 : session.getLeaveCount()) + 1);
        this.updateById(session);
        return reserve;
    }

    @Override
    public LiveStartNotifyResult notifyStart(Long id, LiveStartNotifyRequest request) {
        LiveStartNotifyResult result = new LiveStartNotifyResult();
        if (request == null || request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            result.setSkippedCount(0);
            return result;
        }

        LiveSession session = this.getById(id);
        if (session == null) {
            return result;
        }

        List<CustomerInfo> customers = customerInfoService.listByIds(request.getCustomerIds());
        if (customers == null || customers.isEmpty()) {
            result.setSkippedCount(request.getCustomerIds().size());
            return result;
        }

        SysMessageTemplate template = messageTemplateService.getActiveByCode(
                StringUtils.hasText(request.getTemplateCode()) ? request.getTemplateCode() : "LIVE_START");
        String titleTemplate = template == null ? "直播开播通知：{sessionName}" : template.getTitleTemplate();
        String contentTemplate = template == null
                ? "您关注的直播「{sessionName}」已经开始，点击进入观看：{playUrl}"
                : template.getContentTemplate();
        String smsTemplate = template == null
                ? "您关注的直播「{sessionName}」已经开始，观看地址：{playUrl}"
                : template.getSmsTemplate();
        String templateCode = template == null ? "LIVE_START" : template.getTemplateCode();

        List<String> phones = customers.stream()
                .map(CustomerInfo::getPhone)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        Map<String, AppUser> appUserMap = phones.isEmpty() ? Collections.emptyMap()
                : appUserService.lambdaQuery()
                    .in(AppUser::getPhone, phones)
                    .eq(AppUser::getStatus, 1)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(AppUser::getPhone, user -> user, (left, right) -> left));

        boolean sendSystem = Boolean.TRUE.equals(request.getSendSystemMessage());
        boolean sendSms = Boolean.TRUE.equals(request.getSendSms());
        int skipped = 0;
        List<String> skippedCustomers = new ArrayList<>();

        for (CustomerInfo customer : customers) {
            Map<String, Object> variables = buildLiveNotifyVariables(session, customer);
            if (sendSystem) {
                AppUser appUser = appUserMap.get(customer.getPhone());
                if (appUser == null) {
                    skipped++;
                    skippedCustomers.add(customer.getCustomerName() + "(" + customer.getPhone() + ")");
                } else {
                    SysMessage message = new SysMessage();
                    message.setMsgType(2);
                    message.setMsgCode("LIVE_START");
                    message.setTitle(messageTemplateService.render(titleTemplate, variables));
                    message.setContent(messageTemplateService.render(contentTemplate, variables));
                    message.setChannel(MessageConstants.CHANNEL_SYSTEM);
                    message.setLinkUrl("/live/detail/" + session.getId());
                    message.setSendStatus(MessageConstants.SEND_STATUS_PENDING);

                    MessageSendRequest sendRequest = new MessageSendRequest();
                    sendRequest.setMessage(message);
                    sendRequest.setReceiverType(MessageConstants.RECEIVER_TYPE_H5);
                    sendRequest.setTargetType(MessageConstants.TARGET_TYPE_SELECTED);
                    sendRequest.setReceiverIds(Collections.singletonList(appUser.getId()));
                    MessageSendResult sendResult = sysMessageService.send(sendRequest);
                    result.setSystemMessageCount(result.getSystemMessageCount() + sendResult.getSuccessCount());
                }
            }
            if (sendSms && StringUtils.hasText(customer.getPhone())) {
                String smsContent = messageTemplateService.render(smsTemplate, variables);
                if (smsService.sendSms(customer.getPhone(), templateCode, smsContent)) {
                    result.setSmsCount(result.getSmsCount() + 1);
                }
            }
        }

        result.setSkippedCount(skipped);
        result.setSkippedCustomers(skippedCustomers);
        return result;
    }

    private Map<String, Object> buildLiveNotifyVariables(LiveSession session, CustomerInfo customer) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("sessionName", session.getSessionName());
        variables.put("anchorName", session.getAnchorName());
        variables.put("startTime", session.getActualStartTime() == null ? session.getStartTime() : session.getActualStartTime());
        variables.put("playUrl", "/live/detail/" + session.getId());
        variables.put("customerName", customer.getCustomerName());
        return variables;
    }

    private LiveSession getByStreamId(String streamId) {
        return this.getOne(new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getStreamId, streamId)
                .eq(LiveSession::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private String readString(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private Integer readInteger(Map<String, Object> payload, String... keys) {
        String value = readString(payload, keys);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long readLong(Map<String, Object> payload, String key) {
        String value = readString(payload, key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
