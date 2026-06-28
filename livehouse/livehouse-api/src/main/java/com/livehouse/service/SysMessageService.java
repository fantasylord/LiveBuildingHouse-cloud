package com.livehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.dto.MessageSendRequest;
import com.livehouse.dto.MessageSendResult;
import com.livehouse.entity.SysMessage;

import java.util.List;

public interface SysMessageService extends IService<SysMessage> {

    Page<SysMessage> listPage(int pageNum, int pageSize, String messageType, Integer status);

    boolean sendMessage(Long id);

    boolean batchSend(Long[] ids);

    boolean broadcastByRole(Long roleId, SysMessage message);

    boolean broadcastToUsers(Long[] userIds, SysMessage message);

    boolean broadcastToAll(SysMessage message);

    MessageSendResult send(MessageSendRequest request);

    List<SysMessage> getReceivedMessages(Long receiverId, Integer readStatus, int pageNum, int pageSize);

    Page<SysMessage> getReceivedMessagePage(Long receiverId, Integer receiverType, Integer readStatus, int pageNum, int pageSize);

    boolean markAsRead(Long messageId, Long receiverId);

    boolean markAsRead(Long messageId, Long receiverId, Integer receiverType);

    long getUnreadCount(Long receiverId);

    long getUnreadCount(Long receiverId, Integer receiverType);
}
