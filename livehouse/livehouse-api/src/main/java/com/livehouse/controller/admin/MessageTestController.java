package com.livehouse.controller.admin;

import com.livehouse.entity.SysMessage;
import com.livehouse.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/message")
@RequiredArgsConstructor
public class MessageTestController {

    private final SysMessageService sysMessageService;

    @PostMapping("/create")
    public String createTestMessage() {
        SysMessage message = new SysMessage();
        message.setMsgType(4);
        message.setTitle("测试系统消息");
        message.setContent("这是一条测试消息，用于验证消息管理功能是否正常工作。");
        message.setChannel("system");
        message.setSenderId(1L);
        message.setSenderName("系统管理员");
        
        boolean saved = sysMessageService.save(message);
        return saved ? "消息创建成功，ID: " + message.getId() : "消息创建失败";
    }

    @PostMapping("/broadcast/all")
    public String broadcastToAll() {
        SysMessage message = new SysMessage();
        message.setMsgType(4);
        message.setTitle("全员广播测试");
        message.setContent("这是一条全员广播测试消息，所有用户都应该能收到。");
        message.setChannel("system");
        message.setSenderId(1L);
        message.setSenderName("系统管理员");
        
        boolean result = sysMessageService.broadcastToAll(message);
        return result ? "全员广播成功，消息ID: " + message.getId() : "全员广播失败";
    }

    @GetMapping("/unread/{receiverId}")
    public String getUnreadCount(@PathVariable Long receiverId) {
        long count = sysMessageService.getUnreadCount(receiverId);
        return "用户 " + receiverId + " 的未读消息数量: " + count;
    }
}