package com.livehouse.dto;

import lombok.Data;

import java.util.List;

@Data
public class LiveStartNotifyRequest {

    private List<Long> customerIds;

    private String templateCode;

    private Boolean sendSystemMessage;

    private Boolean sendSms;
}
