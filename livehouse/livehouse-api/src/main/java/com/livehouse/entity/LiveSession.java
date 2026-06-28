package com.livehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 直播场次表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("live_session")
public class LiveSession extends BaseEntity {

    private String sessionName;

    private String sessionCode;

    private String coverImage;
    private Long buildingId;

    private Long unitId;

    private Long platformId;

    private Integer liveType;

    private String streamId;

    private String pushUrl;

    private String playUrl;

    private Long anchorId;

    private String anchorName;

    private String password;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Integer status;

    private Integer maxViewer;

    private Integer totalViewer;

    private String replayUrl;

    private Integer replayStatus;

    private Integer reserveCount;

    private Integer leaveCount;

    private String introduction;
}