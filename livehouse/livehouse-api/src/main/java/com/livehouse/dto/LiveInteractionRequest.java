package com.livehouse.dto;

import lombok.Data;

@Data
public class LiveInteractionRequest {

    private String clientId;

    private String content;

    private String nickname;
}
