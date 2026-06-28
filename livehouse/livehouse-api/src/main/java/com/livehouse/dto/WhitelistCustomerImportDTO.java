package com.livehouse.dto;

import lombok.Data;

import java.util.List;

@Data
public class WhitelistCustomerImportDTO {

    private Long sessionId;

    private List<Long> customerIds;

    private String remark;
}
