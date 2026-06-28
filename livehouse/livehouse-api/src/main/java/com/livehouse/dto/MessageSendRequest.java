package com.livehouse.dto;

import com.livehouse.entity.SysMessage;
import lombok.Data;

import java.util.List;

@Data
public class MessageSendRequest {

    private SysMessage message;

    /**
     * 1-H5 app user, 2-admin sys user.
     */
    private Integer receiverType;

    /**
     * selected/all.
     */
    private String targetType;

    private List<Long> receiverIds;

    /**
     * CRM customer ids. Used for H5 messages and matched to app_user by phone.
     */
    private List<Long> customerIds;
}
