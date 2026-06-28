package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson2.TypeReference;
import com.livehouse.common.constants.MessageConstants;
import com.livehouse.common.constants.SystemConstants;
import com.livehouse.dto.MessageSendRequest;
import com.livehouse.dto.MessageSendResult;
import com.livehouse.entity.AppUser;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.entity.SysMessage;
import com.livehouse.entity.SysMessageReceiver;
import com.livehouse.entity.SysUser;
import com.livehouse.mapper.SysMessageMapper;
import com.livehouse.service.AppUserService;
import com.livehouse.service.CustomerInfoService;
import com.livehouse.service.MessagePushService;
import com.livehouse.service.SysMessageReceiverService;
import com.livehouse.service.SysMessageService;
import com.livehouse.service.SysUserService;
import com.livehouse.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    private final MessagePushService messagePushService;
    private final SysMessageReceiverService messageReceiverService;
    private final SysUserService sysUserService;
    private final AppUserService appUserService;
    private final CustomerInfoService customerInfoService;

    @Override
    public Page<SysMessage> listPage(int pageNum, int pageSize, String messageType, Integer status) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(messageType)) {
            wrapper.eq(SysMessage::getMsgType, messageType);
        }
        if (status != null) {
            wrapper.eq(SysMessage::getSendStatus, status);
        }
        wrapper.orderByDesc(SysMessage::getCreateTime);
        Page<SysMessage> page = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(message -> {
            int count = Math.toIntExact(messageReceiverService.lambdaQuery()
                    .eq(SysMessageReceiver::getMessageId, message.getId())
                    .count());
            message.setReceiverCount(count);
        });
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendMessage(Long id) {
        SysMessage message = getById(id);
        if (message == null || isSendStatus(message, MessageConstants.SEND_STATUS_SENT)) {
            return false;
        }

        MessageSendRequest request = parseRequest(message);
        if (request == null) {
            markFailed(message);
            return false;
        }

        request.setMessage(message);
        MessageSendResult result = send(request);
        return result.getSuccessCount() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSend(Long[] ids) {
        boolean success = true;
        for (Long id : ids) {
            success = sendMessage(id) && success;
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean broadcastByRole(Long roleId, SysMessage message) {
        List<SysUser> users = sysUserService.lambdaQuery()
                .eq(SysUser::getRoleId, roleId)
                .eq(SysUser::getStatus, SystemConstants.STATUS_ENABLE)
                .list();
        return broadcastToUsers(users.stream().map(SysUser::getId).toArray(Long[]::new), message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean broadcastToUsers(Long[] userIds, SysMessage message) {
        MessageSendRequest request = new MessageSendRequest();
        request.setMessage(message);
        request.setReceiverType(MessageConstants.RECEIVER_TYPE_ADMIN);
        request.setTargetType(MessageConstants.TARGET_TYPE_SELECTED);
        request.setReceiverIds(Arrays.asList(userIds));
        return send(request).getSuccessCount() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean broadcastToAll(SysMessage message) {
        MessageSendRequest request = new MessageSendRequest();
        request.setMessage(message);
        request.setReceiverType(MessageConstants.RECEIVER_TYPE_ADMIN);
        request.setTargetType(MessageConstants.TARGET_TYPE_ALL);
        return send(request).getSuccessCount() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageSendResult send(MessageSendRequest request) {
        MessageSendResult result = new MessageSendResult();
        if (request == null || request.getMessage() == null || request.getReceiverType() == null) {
            return result;
        }
        request.setTargetType(normalizeTargetType(request.getTargetType()));

        SysMessage message = request.getMessage();
        if (message.getId() == null) {
            prepareNewMessage(message);
            save(message);
        }

        List<SysMessageReceiver> receivers = resolveReceivers(request, result);
        result.setMessageId(message.getId());

        if (receivers.isEmpty()) {
            markFailed(message);
            saveSendMeta(message, request, result);
            return result;
        }

        int count = messagePushService.pushMessage(message, request.getReceiverType(), receivers);
        result.setSuccessCount(count);

        message.setSendStatus(MessageConstants.SEND_STATUS_SENT);
        message.setSendTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        saveSendMeta(message, request, result);
        return result;
    }

    @Override
    public List<SysMessage> getReceivedMessages(Long receiverId, Integer readStatus, int pageNum, int pageSize) {
        return getReceivedMessagePage(receiverId, null, readStatus, pageNum, pageSize).getRecords();
    }

    @Override
    public Page<SysMessage> getReceivedMessagePage(Long receiverId, Integer receiverType, Integer readStatus, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysMessageReceiver> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageReceiver::getReceiverId, receiverId);
        if (receiverType != null) {
            wrapper.eq(SysMessageReceiver::getReceiverType, receiverType);
        }
        if (readStatus != null) {
            wrapper.eq(SysMessageReceiver::getReadStatus, readStatus);
        }
        wrapper.orderByDesc(SysMessageReceiver::getCreateTime);

        Page<SysMessageReceiver> receiverPage = messageReceiverService.page(new Page<>(pageNum, pageSize), wrapper);
        List<Long> messageIds = receiverPage.getRecords().stream()
                .map(SysMessageReceiver::getMessageId)
                .collect(Collectors.toList());

        Page<SysMessage> page = new Page<>(pageNum, pageSize, receiverPage.getTotal());
        if (messageIds.isEmpty()) {
            page.setRecords(Collections.emptyList());
            return page;
        }

        Map<Long, SysMessage> messageMap = listByIds(messageIds).stream()
                .collect(Collectors.toMap(SysMessage::getId, item -> item));
        List<SysMessage> records = new ArrayList<>();
        for (SysMessageReceiver receiver : receiverPage.getRecords()) {
            SysMessage message = messageMap.get(receiver.getMessageId());
            if (message != null) {
                message.setReadStatus(receiver.getReadStatus());
                message.setReadTime(receiver.getReadTime());
                message.setReceiverType(receiver.getReceiverType());
                records.add(message);
            }
        }
        page.setRecords(records);
        return page;
    }

    @Override
    public boolean markAsRead(Long messageId, Long receiverId) {
        return messageReceiverService.markAsRead(messageId, receiverId);
    }

    @Override
    public boolean markAsRead(Long messageId, Long receiverId, Integer receiverType) {
        return messageReceiverService.markAsRead(messageId, receiverId, receiverType);
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        return messageReceiverService.countUnread(receiverId);
    }

    @Override
    public long getUnreadCount(Long receiverId, Integer receiverType) {
        return messageReceiverService.countUnread(receiverId, receiverType);
    }

    private List<SysMessageReceiver> resolveReceivers(MessageSendRequest request, MessageSendResult result) {
        int receiverType = request.getReceiverType();
        if (receiverType == MessageConstants.RECEIVER_TYPE_ADMIN) {
            return resolveAdminReceivers(request);
        }
        if (receiverType == MessageConstants.RECEIVER_TYPE_H5) {
            return resolveH5Receivers(request, result);
        }
        return Collections.emptyList();
    }

    private List<SysMessageReceiver> resolveAdminReceivers(MessageSendRequest request) {
        List<SysUser> users;
        if (MessageConstants.TARGET_TYPE_ALL.equals(request.getTargetType())) {
            users = sysUserService.lambdaQuery()
                    .eq(SysUser::getStatus, SystemConstants.STATUS_ENABLE)
                    .list();
        } else {
            if (CollectionUtils.isEmpty(request.getReceiverIds())) {
                return Collections.emptyList();
            }
            users = sysUserService.lambdaQuery()
                    .in(SysUser::getId, request.getReceiverIds())
                    .eq(SysUser::getStatus, SystemConstants.STATUS_ENABLE)
                    .list();
        }

        Map<Long, SysMessageReceiver> receiverMap = new LinkedHashMap<>();
        for (SysUser user : users) {
            SysMessageReceiver receiver = new SysMessageReceiver();
            receiver.setReceiverId(user.getId());
            receiver.setReceiverName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
            receiverMap.put(user.getId(), receiver);
        }
        return new ArrayList<>(receiverMap.values());
    }

    private List<SysMessageReceiver> resolveH5Receivers(MessageSendRequest request, MessageSendResult result) {
        if (MessageConstants.TARGET_TYPE_ALL.equals(request.getTargetType())) {
            List<AppUser> users = appUserService.lambdaQuery()
                    .eq(AppUser::getStatus, SystemConstants.STATUS_ENABLE)
                    .list();
            return buildAppUserReceivers(users);
        }

        if (!CollectionUtils.isEmpty(request.getCustomerIds())) {
            return resolveCustomerReceivers(request.getCustomerIds(), result);
        }

        if (CollectionUtils.isEmpty(request.getReceiverIds())) {
            return Collections.emptyList();
        }

        List<AppUser> users = appUserService.lambdaQuery()
                .in(AppUser::getId, request.getReceiverIds())
                .eq(AppUser::getStatus, SystemConstants.STATUS_ENABLE)
                .list();
        return buildAppUserReceivers(users);
    }

    private List<SysMessageReceiver> resolveCustomerReceivers(List<Long> customerIds, MessageSendResult result) {
        List<CustomerInfo> customers = customerInfoService.listByIds(customerIds);
        List<String> phones = customers.stream()
                .map(CustomerInfo::getPhone)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Map<String, AppUser> appUserMap = phones.isEmpty() ? Collections.emptyMap() :
                appUserService.lambdaQuery()
                        .in(AppUser::getPhone, phones)
                        .eq(AppUser::getStatus, SystemConstants.STATUS_ENABLE)
                        .list()
                        .stream()
                        .collect(Collectors.toMap(AppUser::getPhone, user -> user, (left, right) -> left));

        Map<Long, SysMessageReceiver> receiverMap = new LinkedHashMap<>();
        for (CustomerInfo customer : customers) {
            AppUser appUser = appUserMap.get(customer.getPhone());
            if (appUser == null) {
                result.getSkippedCustomers().add(customer.getCustomerName() + "(" + customer.getPhone() + ")");
                continue;
            }
            SysMessageReceiver receiver = new SysMessageReceiver();
            receiver.setReceiverId(appUser.getId());
            receiver.setReceiverName(StringUtils.hasText(appUser.getRealName()) ? appUser.getRealName() : customer.getCustomerName());
            receiverMap.put(appUser.getId(), receiver);
        }
        result.setSkippedCount(result.getSkippedCustomers().size());
        return new ArrayList<>(receiverMap.values());
    }

    private List<SysMessageReceiver> buildAppUserReceivers(List<AppUser> users) {
        Map<Long, SysMessageReceiver> receiverMap = new LinkedHashMap<>();
        for (AppUser user : users) {
            SysMessageReceiver receiver = new SysMessageReceiver();
            receiver.setReceiverId(user.getId());
            receiver.setReceiverName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
            receiverMap.put(user.getId(), receiver);
        }
        return new ArrayList<>(receiverMap.values());
    }

    private void prepareNewMessage(SysMessage message) {
        if (message.getSendStatus() == null) {
            message.setSendStatus(MessageConstants.SEND_STATUS_PENDING);
        }
        if (!StringUtils.hasText(message.getChannel())) {
            message.setChannel(MessageConstants.CHANNEL_SYSTEM);
        }
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        message.setDeleted(0);
    }

    private void markFailed(SysMessage message) {
        message.setSendStatus(MessageConstants.SEND_STATUS_FAILED);
        message.setUpdateTime(LocalDateTime.now());
        updateById(message);
    }

    private boolean isSendStatus(SysMessage message, int expectedStatus) {
        return message.getSendStatus() != null && message.getSendStatus() == expectedStatus;
    }

    private String normalizeTargetType(String targetType) {
        if (!StringUtils.hasText(targetType)) {
            return MessageConstants.TARGET_TYPE_SELECTED;
        }
        String normalized = targetType.trim().toLowerCase();
        if (MessageConstants.TARGET_TYPE_ALL.equals(normalized)) {
            return MessageConstants.TARGET_TYPE_ALL;
        }
        return MessageConstants.TARGET_TYPE_SELECTED;
    }

    private void saveSendMeta(SysMessage message, MessageSendRequest request, MessageSendResult result) {
        try {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("receiverType", request.getReceiverType());
            meta.put("targetType", request.getTargetType());
            meta.put("receiverIds", request.getReceiverIds());
            meta.put("customerIds", request.getCustomerIds());
            meta.put("successCount", result.getSuccessCount());
            meta.put("skippedCount", result.getSkippedCount());
            meta.put("skippedCustomers", result.getSkippedCustomers());
            message.setExtData(JsonUtils.toJsonString(meta));
        } catch (Exception e) {
            log.warn("Message send meta serialization failed: {}", e.getMessage());
        }
        updateById(message);
    }

    private MessageSendRequest parseRequest(SysMessage message) {
        if (!StringUtils.hasText(message.getExtData())) {
            return null;
        }
        try {
            return JsonUtils.parseObject(message.getExtData(), MessageSendRequest.class);
        } catch (Exception e) {
            try {
                Map<String, Object> meta = JsonUtils.parseObject(message.getExtData(), new TypeReference<Map<String, Object>>() {});
                MessageSendRequest request = new MessageSendRequest();
                Object receiverType = meta.get("receiverType");
                if (receiverType != null) {
                    request.setReceiverType(Integer.valueOf(receiverType.toString()));
                }
                Object targetType = meta.get("targetType");
                if (targetType != null) {
                    request.setTargetType(targetType.toString());
                }
                request.setReceiverIds(toLongList(meta.get("receiverIds")));
                request.setCustomerIds(toLongList(meta.get("customerIds")));
                return request;
            } catch (Exception ignored) {
                log.warn("Message send request parse failed - messageId: {}, error: {}", message.getId(), e.getMessage());
                return null;
            }
        }
    }

    private List<Long> toLongList(Object value) {
        if (!(value instanceof List<?>)) {
            return null;
        }
        List<?> list = (List<?>) value;
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> Long.valueOf(item.toString()))
                .collect(Collectors.toList());
    }
}
