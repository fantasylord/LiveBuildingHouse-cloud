package com.livehouse.service;

import com.livehouse.entity.SysMessage;
import com.livehouse.entity.SysMessageReceiver;

import java.util.List;

public interface MessagePushService {

    void pushMessage(SysMessage message, Long receiverId);

    void broadcastMessage(SysMessage message, Long[] receiverIds);

    int pushMessage(SysMessage message, Integer receiverType, List<SysMessageReceiver> receivers);
}
