package com.livehouse.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageSendResult {

    private Long messageId;

    private Integer successCount = 0;

    private Integer skippedCount = 0;

    private List<String> skippedCustomers = new ArrayList<>();
}
