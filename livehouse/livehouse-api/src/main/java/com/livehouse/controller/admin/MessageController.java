package com.livehouse.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.constants.MessageConstants;
import com.livehouse.common.result.Result;
import com.livehouse.dto.MessageSendRequest;
import com.livehouse.dto.MessageSendResult;
import com.livehouse.entity.SysMessage;
import com.livehouse.service.SysMessageService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    private final SysMessageService sysMessageService;

    @GetMapping({"", "/list"})
    public Result<PageResponse<SysMessage>> list(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "messageType", required = false) String messageType,
            @RequestParam(value = "status", required = false) Integer status) {
        Page<SysMessage> page = sysMessageService.listPage(pageNum, pageSize, messageType, status);
        return Result.success(PageUtil.buildPageResponse(page));
    }

    @GetMapping("/{id}")
    public Result<SysMessage> detail(@PathVariable Long id) {
        return Result.success(sysMessageService.getById(id));
    }

    @PostMapping("")
    public Result<Void> add(@RequestBody SysMessage message) {
        if (message.getSendStatus() == null) {
            message.setSendStatus(MessageConstants.SEND_STATUS_PENDING);
        }
        return sysMessageService.save(message) ? Result.success() : Result.error("新增消息失败");
    }

    @PostMapping("/send")
    public Result<MessageSendResult> send(@RequestBody MessageSendRequest request) {
        MessageSendResult result = sysMessageService.send(request);
        return result.getSuccessCount() > 0 ? Result.success(result) : Result.error("发送消息失败");
    }

    @PutMapping("")
    public Result<Void> update(@RequestBody SysMessage message) {
        return sysMessageService.updateById(message) ? Result.success() : Result.error("更新消息失败");
    }

    @PutMapping("/{id}/send")
    public Result<Void> send(@PathVariable Long id) {
        return sysMessageService.sendMessage(id) ? Result.success() : Result.error("发送消息失败");
    }

    @PutMapping("/batch/send")
    public Result<Void> batchSend(@RequestBody Long[] ids) {
        return sysMessageService.batchSend(ids) ? Result.success() : Result.error("批量发送失败");
    }

    @PostMapping("/broadcast/role/{roleId}")
    public Result<Void> broadcastByRole(@PathVariable Long roleId, @RequestBody SysMessage message) {
        return sysMessageService.broadcastByRole(roleId, message) ? Result.success() : Result.error("按角色广播失败");
    }

    @PostMapping("/broadcast/users")
    public Result<Void> broadcastToUsers(@RequestBody BroadcastRequest request) {
        return sysMessageService.broadcastToUsers(request.getUserIds(), request.getMessage())
                ? Result.success() : Result.error("按用户广播失败");
    }

    @PostMapping("/broadcast/all")
    public Result<Void> broadcastToAll(@RequestBody SysMessage message) {
        return sysMessageService.broadcastToAll(message) ? Result.success() : Result.error("全员广播失败");
    }

    @GetMapping("/received")
    public Result<List<SysMessage>> getReceivedMessages(
            @RequestParam(value = "receiverId") Long receiverId,
            @RequestParam(value = "receiverType", required = false) Integer receiverType,
            @RequestParam(value = "readStatus", required = false) Integer readStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<SysMessage> messages = sysMessageService
                .getReceivedMessagePage(receiverId, receiverType, readStatus, pageNum, pageSize)
                .getRecords();
        return Result.success(messages);
    }

    @PutMapping("/{messageId}/read/{receiverId}")
    public Result<Void> markAsRead(
            @PathVariable Long messageId,
            @PathVariable Long receiverId,
            @RequestParam(value = "receiverType", required = false) Integer receiverType) {
        return sysMessageService.markAsRead(messageId, receiverId, receiverType)
                ? Result.success() : Result.error("标记已读失败");
    }

    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(
            @RequestParam(value = "receiverId") Long receiverId,
            @RequestParam(value = "receiverType", required = false) Integer receiverType) {
        return Result.success(sysMessageService.getUnreadCount(receiverId, receiverType));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return sysMessageService.removeById(id) ? Result.success() : Result.error("删除消息失败");
    }

    public static class BroadcastRequest {
        private Long[] userIds;
        private SysMessage message;

        public Long[] getUserIds() {
            return userIds;
        }

        public void setUserIds(Long[] userIds) {
            this.userIds = userIds;
        }

        public SysMessage getMessage() {
            return message;
        }

        public void setMessage(SysMessage message) {
            this.message = message;
        }
    }
}
