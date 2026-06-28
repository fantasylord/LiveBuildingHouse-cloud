package com.livehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.entity.SysMessageReceiver;

public interface SysMessageReceiverService extends IService<SysMessageReceiver> {

    boolean markAsRead(Long messageId, Long receiverId);

    boolean markAsRead(Long messageId, Long receiverId, Integer receiverType);

    long countUnread(Long receiverId);

    long countUnread(Long receiverId, Integer receiverType);
}
