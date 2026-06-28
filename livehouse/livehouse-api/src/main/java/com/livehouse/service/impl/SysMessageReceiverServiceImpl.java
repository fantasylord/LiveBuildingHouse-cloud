package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.common.constants.MessageConstants;
import com.livehouse.entity.SysMessageReceiver;
import com.livehouse.mapper.SysMessageReceiverMapper;
import com.livehouse.service.SysMessageReceiverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysMessageReceiverServiceImpl extends ServiceImpl<SysMessageReceiverMapper, SysMessageReceiver> implements SysMessageReceiverService {

    @Override
    public boolean markAsRead(Long messageId, Long receiverId) {
        return markAsRead(messageId, receiverId, null);
    }

    @Override
    public boolean markAsRead(Long messageId, Long receiverId, Integer receiverType) {
        LambdaUpdateWrapper<SysMessageReceiver> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMessageReceiver::getMessageId, messageId)
                .eq(SysMessageReceiver::getReceiverId, receiverId)
                .set(SysMessageReceiver::getReadStatus, MessageConstants.READ_STATUS_READ)
                .set(SysMessageReceiver::getReadTime, LocalDateTime.now());
        if (receiverType != null) {
            wrapper.eq(SysMessageReceiver::getReceiverType, receiverType);
        }
        return update(wrapper);
    }

    @Override
    public long countUnread(Long receiverId) {
        return countUnread(receiverId, null);
    }

    @Override
    public long countUnread(Long receiverId, Integer receiverType) {
        if (receiverType == null) {
            return lambdaQuery()
                    .eq(SysMessageReceiver::getReceiverId, receiverId)
                    .eq(SysMessageReceiver::getReadStatus, MessageConstants.READ_STATUS_UNREAD)
                    .count();
        }
        return lambdaQuery()
                .eq(SysMessageReceiver::getReceiverId, receiverId)
                .eq(SysMessageReceiver::getReceiverType, receiverType)
                .eq(SysMessageReceiver::getReadStatus, MessageConstants.READ_STATUS_UNREAD)
                .count();
    }
}
