USE livehouse;

CREATE TABLE IF NOT EXISTS home_config (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    config_key      VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键: banners/quick_entry/notice',
    config_name     VARCHAR(100) COMMENT '配置名称',
    config_type     VARCHAR(20) DEFAULT 'json' COMMENT '配置类型: json/text/image',
    config_value    TEXT COMMENT '配置值',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    sort_order      INT DEFAULT 0 COMMENT '排序号',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_config_key (config_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页配置表';

INSERT INTO home_config (config_key, config_name, config_type, config_value, status, sort_order) VALUES
('banners', '首页Banner', 'json', '[
    {"id":1,"title":"星河湾花园","coverImage":"https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20real%20estate%20banner%20with%20modern%20residential%20building%20sunset%20sky&image_size=landscape_16_9","linkType":"house","linkId":1,"sortOrder":1},
    {"id":2,"title":"绿城玫瑰园","coverImage":"https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20villa%20estate%20garden%20beautiful%20landscape&image_size=landscape_16_9","linkType":"house","linkId":2,"sortOrder":2},
    {"id":3,"title":"直播带看专场","coverImage":"https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20real%20estate%20show%20professional%20anchor&image_size=landscape_16_9","linkType":"live","linkId":1,"sortOrder":3}
]', 1, 1),
('quick_entry', '快捷入口', 'json', '[
    {"id":1,"icon":"home-o","text":"楼盘","path":"/house/list","sortOrder":1},
    {"id":2,"icon":"video-o","text":"直播","path":"/live/list","sortOrder":2},
    {"id":3,"icon":"calendar-o","text":"预约","path":"/reserve","sortOrder":3},
    {"id":4,"icon":"user-o","text":"我的","path":"/profile","sortOrder":4}
]', 1, 2),
('notice', '首页公告', 'text', '欢迎来到LiveHouse智慧看房平台，最新楼盘优惠活动正在进行中！', 1, 3);

INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order) VALUES
(1, '首页配置', 'menu', '/system/home-config', 'system/HomeConfigList', 'system:homeConfig:list', 'HomeFilled', 4);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, (SELECT id FROM sys_menu WHERE menu_name = '首页配置'));

COMMIT;