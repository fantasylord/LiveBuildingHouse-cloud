package com.livehouse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播地址信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveAddress {

    private String pushUrl;

    private String playUrl;

    private String flvUrl;

    private String hlsUrl;

    private String streamId;
}