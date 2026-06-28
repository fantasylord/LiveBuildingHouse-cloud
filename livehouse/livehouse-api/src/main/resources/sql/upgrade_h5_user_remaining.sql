USE livehouse;

DROP PROCEDURE IF EXISTS upgrade_h5_user_remaining;
DELIMITER //
CREATE PROCEDURE upgrade_h5_user_remaining()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'house_reserve'
          AND COLUMN_NAME = 'app_user_id'
    ) THEN
        ALTER TABLE house_reserve ADD COLUMN app_user_id BIGINT COMMENT 'H5用户ID' AFTER id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'house_reserve'
          AND INDEX_NAME = 'idx_app_user_id'
    ) THEN
        ALTER TABLE house_reserve ADD INDEX idx_app_user_id (app_user_id);
    END IF;
END //
DELIMITER ;

CALL upgrade_h5_user_remaining();
DROP PROCEDURE IF EXISTS upgrade_h5_user_remaining;

CREATE TABLE IF NOT EXISTS app_browse_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT NOT NULL COMMENT 'H5用户ID',
    target_type     VARCHAR(20) NOT NULL COMMENT '对象类型: house/live/vr',
    target_id       BIGINT NOT NULL COMMENT '对象ID',
    target_title    VARCHAR(200) COMMENT '展示标题',
    target_cover    VARCHAR(500) COMMENT '封面图',
    target_desc     VARCHAR(500) COMMENT '展示描述',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='H5浏览记录表';

CREATE TABLE IF NOT EXISTS app_favorite (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT NOT NULL COMMENT 'H5用户ID',
    target_type     VARCHAR(20) NOT NULL COMMENT '对象类型: house/live/vr',
    target_id       BIGINT NOT NULL COMMENT '对象ID',
    target_title    VARCHAR(200) COMMENT '展示标题',
    target_cover    VARCHAR(500) COMMENT '封面图',
    target_desc     VARCHAR(500) COMMENT '展示描述',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_user_id (user_id),
    INDEX idx_target (target_type, target_id),
    UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='H5收藏表';

CREATE TABLE IF NOT EXISTS app_user_setting (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id         BIGINT NOT NULL COMMENT 'H5用户ID',
    message_notify  TINYINT DEFAULT 1 COMMENT '站内消息通知: 0-关闭 1-开启',
    sms_notify      TINYINT DEFAULT 1 COMMENT '短信通知: 0-关闭 1-开启',
    browse_history  TINYINT DEFAULT 1 COMMENT '浏览记录: 0-关闭 1-开启',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='H5用户设置表';
