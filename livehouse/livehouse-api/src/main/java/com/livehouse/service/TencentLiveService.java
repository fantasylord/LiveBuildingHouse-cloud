package com.livehouse.service;

import com.livehouse.entity.LiveSession;

public interface TencentLiveService {

    Integer queryStreamStatus(LiveSession session);
}
