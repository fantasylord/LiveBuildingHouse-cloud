CREATE TABLE IF NOT EXISTS live_interaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'primary key',
    session_id BIGINT NOT NULL COMMENT 'live session id',
    app_user_id BIGINT COMMENT 'h5 app user id',
    client_id VARCHAR(100) COMMENT 'guest client id',
    interaction_type TINYINT NOT NULL COMMENT '1-like 2-danmu',
    content VARCHAR(200) COMMENT 'danmu content',
    nickname VARCHAR(50) COMMENT 'display nickname',
    status TINYINT DEFAULT 1 COMMENT '0-inactive 1-active',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    create_by BIGINT COMMENT 'create user',
    update_by BIGINT COMMENT 'update user',
    deleted TINYINT DEFAULT 0 COMMENT 'logical delete flag',
    INDEX idx_session_type (session_id, interaction_type),
    INDEX idx_session_create_time (session_id, create_time),
    INDEX idx_user_like (session_id, app_user_id, interaction_type),
    INDEX idx_client_like (session_id, client_id, interaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='live interaction';

CREATE TABLE IF NOT EXISTS sys_message_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'primary key',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'template code',
    template_name VARCHAR(100) NOT NULL COMMENT 'template name',
    msg_type TINYINT DEFAULT 2 COMMENT 'message type',
    title_template VARCHAR(200) COMMENT 'system message title template',
    content_template TEXT COMMENT 'system message content template',
    sms_template VARCHAR(500) COMMENT 'sms content template',
    variables VARCHAR(500) COMMENT 'available variables',
    status TINYINT DEFAULT 1 COMMENT '0-disabled 1-enabled',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    create_by BIGINT COMMENT 'create user',
    update_by BIGINT COMMENT 'update user',
    deleted TINYINT DEFAULT 0 COMMENT 'logical delete flag',
    INDEX idx_template_code (template_code),
    INDEX idx_msg_type (msg_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='message template';

INSERT INTO sys_message_template (
    template_code,
    template_name,
    msg_type,
    title_template,
    content_template,
    sms_template,
    variables,
    status
) SELECT
    'LIVE_START',
    '直播开播通知',
    2,
    '直播开播通知：{sessionName}',
    '尊敬的{customerName}，您关注的直播《{sessionName}》已经开始，主播：{anchorName}，点击进入观看：{playUrl}',
    '尊敬的{customerName}，您关注的直播《{sessionName}》已经开始，观看地址：{playUrl}',
    'sessionName,anchorName,startTime,playUrl,customerName',
    1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_message_template WHERE template_code = 'LIVE_START'
);
