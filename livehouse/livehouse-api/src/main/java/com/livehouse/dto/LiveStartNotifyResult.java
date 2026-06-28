package com.livehouse.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LiveStartNotifyResult {

    private Integer systemMessageCount = 0;

    private Integer smsCount = 0;

    private Integer skippedCount = 0;

    private List<String> skippedCustomers = new ArrayList<>();
}
