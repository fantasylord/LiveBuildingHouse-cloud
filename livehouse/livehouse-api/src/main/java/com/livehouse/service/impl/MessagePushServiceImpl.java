package com.livehouse.service.impl;

import com.livehouse.entity.SysMessage;
import com.livehouse.entity.SysMessageReceiver;
import com.livehouse.common.constants.MessageConstants;
import com.livehouse.service.MessagePushService;
import com.livehouse.service.SysMessageReceiverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushServiceImpl implements MessagePushService {

    private final SysMessageReceiverService messageReceiverService;

    @Override
    public void pushMessage(SysMessage message, Long receiverId) {
        SysMessageReceiver receiver = new SysMessageReceiver();
        receiver.setReceiverId(receiverId);
        pushMessage(message, MessageConstants.RECEIVER_TYPE_ADMIN, Collections.singletonList(receiver));
    }

    @Override
    public void broadcastMessage(SysMessage message, Long[] receiverIds) {
        List<SysMessageReceiver> receivers = new ArrayList<>();
        for (Long receiverId : receiverIds) {
            SysMessageReceiver receiver = new SysMessageReceiver();
            receiver.setReceiverId(receiverId);
            receivers.add(receiver);
        }
        pushMessage(message, MessageConstants.RECEIVER_TYPE_ADMIN, receivers);
    }

    @Override
    public int pushMessage(SysMessage message, Integer receiverType, List<SysMessageReceiver> receivers) {
        if (message == null || message.getId() == null || receivers == null || receivers.isEmpty()) {
            return 0;
        }

        for (SysMessageReceiver receiver : receivers) {
            receiver.setMessageId(message.getId());
            receiver.setReceiverType(receiverType);
            receiver.setReadStatus(MessageConstants.READ_STATUS_UNREAD);
        }

        messageReceiverService.saveBatch(receivers);
        log.info("Message pushed - messageId: {}, receiverType: {}, count: {}",
                message.getId(), receiverType, receivers.size());
        return receivers.size();
    }
}
