-- 创建数据库
CREATE DATABASE IF NOT EXISTS livehouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE livehouse;

-- 员工账号表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(100) NOT NULL COMMENT '密码',
    real_name       VARCHAR(50) COMMENT '真实姓名',
    phone           VARCHAR(20) COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    avatar          VARCHAR(255) COMMENT '头像URL',
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50) COMMENT '最后登录IP',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_role_id (role_id),
    INDEX idx_status (status),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工账号表';

-- 角色权限表
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_name       VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code       VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description     VARCHAR(500) COMMENT '角色描述',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限表';

-- 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    parent_id       BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name       VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_type       VARCHAR(20) COMMENT '菜单类型: menu-菜单 button-按钮',
    path            VARCHAR(255) COMMENT '路由路径',
    component       VARCHAR(255) COMMENT '组件路径',
    permission      VARCHAR(100) COMMENT '权限标识',
    icon            VARCHAR(100) COMMENT '图标',
    sort_order      INT DEFAULT 0 COMMENT '排序号',
    visible         TINYINT DEFAULT 1 COMMENT '是否显示: 0-隐藏 1-显示',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_type (menu_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id         BIGINT NOT NULL COMMENT '角色ID',
    menu_id         BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 房源楼盘表
CREATE TABLE IF NOT EXISTS house_building (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    building_name   VARCHAR(100) NOT NULL COMMENT '楼盘名称',
    building_code   VARCHAR(50) UNIQUE COMMENT '楼盘编码',
    province        VARCHAR(50) COMMENT '省份',
    city            VARCHAR(50) COMMENT '城市',
    district        VARCHAR(50) COMMENT '区域',
    address         VARCHAR(255) COMMENT '详细地址',
    latitude        DECIMAL(10,7) COMMENT '纬度',
    longitude       DECIMAL(10,7) COMMENT '经度',
    cover_image     VARCHAR(255) COMMENT '封面图片URL',
    avg_price       DECIMAL(15,2) COMMENT '均价(元/㎡)',
    min_price       DECIMAL(15,2) COMMENT '最低价(元/㎡)',
    max_price       DECIMAL(15,2) COMMENT '最高价(元/㎡)',
    building_type   VARCHAR(50) COMMENT '楼盘类型: 普通住宅/别墅/公寓/商业',
    decoration_type VARCHAR(50) COMMENT '装修类型: 毛坯/简装/精装',
    developer       VARCHAR(100) COMMENT '开发商',
    property_company VARCHAR(100) COMMENT '物业公司',
    green_rate      DECIMAL(5,2) COMMENT '绿化率(%)',
    plot_ratio      DECIMAL(5,2) COMMENT '容积率',
    total_units     INT COMMENT '总套数',
    description     TEXT COMMENT '楼盘介绍',
    images          TEXT COMMENT '图片集合(JSON数组)',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    privated        TINYINT DEFAULT 0 COMMENT '是否私密: 0-公开 1-私密',
    sort_order      INT DEFAULT 0 COMMENT '排序号',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_city (city),
    INDEX idx_district (district),
    INDEX idx_status (status),
    INDEX idx_privated (privated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源楼盘表';

-- 户型表
CREATE TABLE IF NOT EXISTS house_unit (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    building_id     BIGINT NOT NULL COMMENT '楼盘ID',
    unit_name       VARCHAR(100) COMMENT '户型名称',
    unit_code       VARCHAR(50) UNIQUE COMMENT '户型编码',
    rooms           INT COMMENT '室',
    halls           INT COMMENT '厅',
    bathrooms       INT COMMENT '卫',
    area            DECIMAL(10,2) COMMENT '面积(㎡)',
    price           DECIMAL(15,2) COMMENT '总价(万元)',
    orientation     VARCHAR(50) COMMENT '朝向',
    floor_range     VARCHAR(50) COMMENT '楼层范围',
    total_floor     INT COMMENT '总楼层',
    description     TEXT COMMENT '户型介绍',
    images          TEXT COMMENT '户型图片(JSON数组)',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_building_id (building_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='户型表';

-- VR素材表
CREATE TABLE IF NOT EXISTS house_vr (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    building_id     BIGINT COMMENT '楼盘ID',
    unit_id         BIGINT COMMENT '户型ID',
    vr_name         VARCHAR(100) COMMENT 'VR名称',
    vr_code         VARCHAR(50) UNIQUE COMMENT 'VR编码',
    panorama_url    VARCHAR(500) COMMENT '全景图URL',
    thumbnail_url       VARCHAR(500) COMMENT '缩略图URL',
    hotspots        JSON COMMENT '预留热点配置字段，场景热点以scenes内hotspots为准',
    scenes          JSON COMMENT '场景化VR配置，包含场景列表、户型模型图、热点目标场景等',
    narration_text  TEXT COMMENT '解说文字',
    narration_audio VARCHAR(500) COMMENT '解说音频URL',
    privated        TINYINT DEFAULT 0 COMMENT '是否私密: 0-公开 1-私密',
    access_count    INT DEFAULT 0 COMMENT '访问次数',
    sort_order      INT DEFAULT 0 COMMENT '排序号',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_building_id (building_id),
    INDEX idx_unit_id (unit_id),
    INDEX idx_status (status),
    INDEX idx_privated (privated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VR素材表';

-- 直播场次表
CREATE TABLE IF NOT EXISTS live_session (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    session_name    VARCHAR(100) NOT NULL COMMENT '直播名称',
    session_code    VARCHAR(50) UNIQUE COMMENT '直播编码',
    building_id     BIGINT COMMENT '楼盘ID',
    unit_id         BIGINT COMMENT '户型ID',
    platform_id     BIGINT COMMENT '关联三方直播平台ID',
    live_type       TINYINT DEFAULT 0 COMMENT '直播类型: 0-公开 1-私密',
    stream_id       VARCHAR(100) COMMENT '三方平台StreamId',
    push_url        VARCHAR(500) COMMENT '推流地址',
    play_url        VARCHAR(500) COMMENT '播放地址',
    cover_image     text  COMMENT '封面图片URL',
    anchor_id       BIGINT COMMENT '主播ID',
    anchor_name     VARCHAR(50) COMMENT '主播名称',
    password        VARCHAR(50) COMMENT '观看密码(私密直播)',
    start_time      DATETIME COMMENT '计划开始时间',
    end_time        DATETIME COMMENT '计划结束时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    status          TINYINT DEFAULT 0 COMMENT '状态: 0-未开始 1-直播中 2-已结束 3-已关闭',
    max_viewer      INT DEFAULT 0 COMMENT '最高在线人数',
    total_viewer    INT DEFAULT 0 COMMENT '累计观看人数',
    replay_url      VARCHAR(500) COMMENT '回放地址',
    replay_status   TINYINT DEFAULT 0 COMMENT '录播状态: 0-未录制 1-录制中 2-已完成',
    reserve_count   INT DEFAULT 0 COMMENT '预约人数',
    leave_count     INT DEFAULT 0 COMMENT '留资人数',
    introduction    TEXT COMMENT '直播简介',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_status (status),
    INDEX idx_live_type (live_type),
    INDEX idx_platform_id (platform_id),
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播场次表';

-- 三方直播平台配置表
CREATE TABLE IF NOT EXISTS live_platform (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    platform_name   VARCHAR(50) NOT NULL COMMENT '平台名称',
    platform_code   VARCHAR(50) NOT NULL UNIQUE COMMENT '平台编码: TENCENT/ALIYUN/DOUYIN',
    platform_type   VARCHAR(20) NOT NULL COMMENT '平台类型: tencent/aliyun/douyin',
    app_id          VARCHAR(100) COMMENT '应用ID',
    app_key         VARCHAR(200) COMMENT '应用Key',
    app_secret      VARCHAR(200) COMMENT '应用Secret',
    push_domain     VARCHAR(200) COMMENT '推流域名',
    play_domain     VARCHAR(200) COMMENT '播放域名',
    push_url_template VARCHAR(500) COMMENT '推流地址模板',
    play_url_template VARCHAR(500) COMMENT '播放地址模板',
    expire_time     INT DEFAULT 3600 COMMENT '地址有效期(秒)',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    description     VARCHAR(500) COMMENT '平台描述',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_platform_code (platform_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三方直播平台配置表';

-- 初始化三方直播平台数据
INSERT INTO live_platform (platform_name, platform_code, platform_type, description) VALUES
('腾讯云直播', 'TENCENT', 'tencent', '腾讯云直播服务，支持推拉流、录播、私密权限'),
('阿里云直播', 'ALIYUN', 'aliyun', '阿里云直播服务（可选）'),
('抖音直播', 'DOUYIN', 'douyin', '抖音直播服务（可选）');

-- 直播白名单表
CREATE TABLE IF NOT EXISTS live_whitelist (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    session_id      BIGINT NOT NULL COMMENT '直播场次ID',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    customer_name   VARCHAR(50) COMMENT '客户姓名',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_session_id (session_id),
    INDEX idx_phone (phone),
    UNIQUE KEY uk_session_phone (session_id, phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播白名单表';

-- 客户信息表
CREATE TABLE IF NOT EXISTS customer_info (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_name   VARCHAR(50) NOT NULL COMMENT '客户姓名',
    customer_code   VARCHAR(50) UNIQUE COMMENT '客户编码',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    gender          TINYINT COMMENT '性别: 1-男 2-女',
    age             INT COMMENT '年龄',
    wechat          VARCHAR(50) COMMENT '微信',
    email           VARCHAR(100) COMMENT '邮箱',
    province        VARCHAR(50) COMMENT '省份',
    city            VARCHAR(50) COMMENT '城市',
    district        VARCHAR(50) COMMENT '区域',
    address         VARCHAR(255) COMMENT '详细地址',
    budget          DECIMAL(15,2) COMMENT '预算(万元)',
    interested_unit VARCHAR(100) COMMENT '意向户型',
    customer_source VARCHAR(50) COMMENT '客户来源: 1-预约看房 2-直播留资 3-自然到访 4-渠道推广',
    customer_level  TINYINT DEFAULT 1 COMMENT '客户级别: 1-普通 2-意向 3-VIP',
    intention_status TINYINT DEFAULT 0 COMMENT '意向状态: 0-无意向 1-了解中 2-有意向 3-高意向 4-已成交',
    assign_consultant_id BIGINT COMMENT '分配置业顾问ID',
    assign_time     DATETIME COMMENT '分配时间',
    first_visit_time DATETIME COMMENT '首次到访时间',
    last_follow_time DATETIME COMMENT '最后跟进时间',
    follow_count    INT DEFAULT 0 COMMENT '跟进次数',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_phone (phone),
    INDEX idx_customer_level (customer_level),
    INDEX idx_intention_status (intention_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 看房预约表
CREATE TABLE IF NOT EXISTS house_reserve (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    building_id     BIGINT NOT NULL COMMENT '楼盘ID',
    unit_id         BIGINT COMMENT '户型ID',
    live_session_id BIGINT COMMENT '关联直播场次ID',
    reserve_code    VARCHAR(50) UNIQUE COMMENT '预约编码',
    customer_name   VARCHAR(50) NOT NULL COMMENT '客户姓名',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    visit_time      DATETIME COMMENT '预约到访时间',
    visit_type      TINYINT DEFAULT 0 COMMENT '到访类型: 0-实地到访 1-在线VR',
    visit_status    TINYINT DEFAULT 0 COMMENT '到访状态: 0-待确认 1-已确认 2-已到访 3-取消',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_building_id (building_id),
    INDEX idx_live_session_id (live_session_id),
    INDEX idx_visit_status (visit_status),
    INDEX idx_visit_time (visit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看房预约表';

-- 客户跟进记录表
CREATE TABLE IF NOT EXISTS customer_follow (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    customer_id     BIGINT NOT NULL COMMENT '客户ID',
    consultant_id   BIGINT COMMENT '置业顾问ID',
    follow_type     TINYINT DEFAULT 0 COMMENT '跟进类型: 0-电话 1-微信 2-短信 3-面谈',
    follow_content  TEXT COMMENT '跟进内容',
    intention_level TINYINT DEFAULT 1 COMMENT '意向等级: 1-普通 2-意向 3-高意向',
    next_plan       TEXT COMMENT '下一步计划',
    next_follow_time DATETIME COMMENT '下次跟进时间',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_customer_id (customer_id),
    INDEX idx_consultant_id (consultant_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进记录表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    msg_type        TINYINT NOT NULL COMMENT '消息类型: 1-预约 2-直播 3-客户 4-系统',
    msg_code        VARCHAR(50) COMMENT '消息编码',
    title           VARCHAR(200) NOT NULL COMMENT '消息标题',
    content         TEXT COMMENT '消息内容',
    sender_id       BIGINT COMMENT '发送人ID',
    sender_name     VARCHAR(50) COMMENT '发送人名称',
    link_url        VARCHAR(255) COMMENT '跳转链接',
    link_params     VARCHAR(500) COMMENT '链接参数',
    send_status     TINYINT DEFAULT 0 COMMENT '发送状态: 0-待发送 1-已发送 2-发送失败',
    send_time       DATETIME COMMENT '发送时间',
    channel         VARCHAR(50) DEFAULT 'system' COMMENT '通知渠道: system-系统 sms-短信 wechat-微信 push-推送',
    ext_data        JSON COMMENT '扩展数据',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT COMMENT '创建人',
    update_by       BIGINT COMMENT '更新人',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_msg_type (msg_type),
    INDEX idx_send_status (send_status),
    INDEX idx_send_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 消息接收记录表
CREATE TABLE IF NOT EXISTS sys_message_receiver (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    message_id      BIGINT NOT NULL COMMENT '消息ID',
    receiver_id     BIGINT NOT NULL COMMENT '接收人ID',
    receiver_name   VARCHAR(50) COMMENT '接收人名称',
    receiver_type   TINYINT DEFAULT 1 COMMENT '接收方类型: 1-用户 2-顾问 3-管理员',
    read_status     TINYINT DEFAULT 0 COMMENT '阅读状态: 0-未读 1-已读',
    read_time       DATETIME COMMENT '阅读时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_message_id (message_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_read_status (read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息接收记录表';

-- 短信发送记录表
CREATE TABLE IF NOT EXISTS sms_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    template_code   VARCHAR(50) COMMENT '短信模板编码',
    content         VARCHAR(500) COMMENT '短信内容',
    status          TINYINT DEFAULT 0 COMMENT '发送状态: 0-待发送 1-已发送 2-发送失败',
    error_message   VARCHAR(500) COMMENT '错误信息',
    send_time       DATETIME COMMENT '发送时间',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送记录表';
-- ============================================
-- APP用户表（前台H5用户，与后台sys_user分开）
-- ============================================
CREATE TABLE IF NOT EXISTS app_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username        VARCHAR(50) COMMENT '用户名',
    password        VARCHAR(100) NOT NULL COMMENT '密码',
    real_name       VARCHAR(50) COMMENT '真实姓名',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    avatar          VARCHAR(255) COMMENT '头像URL',
    gender          TINYINT COMMENT '性别: 1-男 2-女',
    province        VARCHAR(50) COMMENT '省份',
    city            VARCHAR(50) COMMENT '城市',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50) COMMENT '最后登录IP',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP用户表（前台H5用户）';

-- 初始化角色数据
INSERT INTO sys_role (role_name, role_code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '全部权限'),
('案场经理', 'CASE_MANAGER', '房源/直播/客户/员工管理'),
('置业顾问', 'CONSULTANT', '预约管理、客户跟进、直播观看'),
('直播主播', 'ANCHOR', '仅开播、关播、查看本场数据');

-- 初始化管理员账号 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, role_id, status) VALUES
('admin', '$2a$10$UP5cXeENgBW/sGFpjmdR8Ok5RQZAEIlian/80P9INm5vWVVnFrhSK', '系统管理员', '13800138000', 1, 1);

-- 初始化菜单数据
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order) VALUES
(0, '系统管理', 'menu', '/system', '', '', 'Setting', 1),
(1, '用户管理', 'menu', '/system/user', 'system/UserList', 'sys:user:list', 'User', 1),
(1, '角色管理', 'menu', '/system/role', 'system/RoleList', 'sys:role:list', 'UserFilled', 2),
(1, '菜单管理', 'menu', '/system/menu', 'system/MenuList', 'sys:menu:list', 'Menu', 3),
(0, '房源管理', 'menu', '/house', '', '', 'Building', 2),
(5, '楼盘管理', 'menu', '/house/building', 'house/BuildingList', 'house:building:list', 'Home', 1),
(5, '户型管理', 'menu', '/house/unit', 'house/UnitList', 'house:unit:list', 'Layout', 2),
(5, 'VR管理', 'menu', '/house/vr', 'house/VrList', 'house:vr:list', 'Video', 3),
(0, '直播管理', 'menu', '/live', '', '', 'Video', 3),
(9, '直播场次', 'menu', '/live/session', 'live/SessionList', 'live:session:list', 'Calendar', 1),
(9, '直播平台', 'menu', '/live/platform', 'live/PlatformList', 'live:platform:list', 'Monitor', 2),
(9, '白名单管理', 'menu', '/live/whitelist', 'live/WhitelistList', 'live:whitelist:list', 'User', 3),
(0, '客户管理', 'menu', '/customer', '', '', 'User', 4),
(12, '客户列表', 'menu', '/customer/list', 'customer/CustomerList', 'customer:list', 'Users', 1),
(12, '跟进记录', 'menu', '/customer/follow', 'customer/FollowList', 'customer:follow:list', 'Message', 2),
(12, '预约管理', 'menu', '/customer/reserve', 'customer/ReserveList', 'customer:reserve:list', 'Calendar', 3),
(0, '数据看板', 'menu', '/dashboard', 'dashboard/Dashboard', 'dashboard:view', 'BarChart3', 0),
(0, '消息中心', 'menu', '/message', 'message/MessageList', 'message:list', 'Message', 5),
(18, '消息管理', 'menu', '/message/list', 'message/MessageList', 'message:list', 'Bell', 1),
(18, '短信记录', 'menu', '/message/sms', 'message/SmsRecordList', 'message:sms:list', 'ChatDotRound', 2);

-- 初始化角色菜单关联数据（超级管理员拥有所有菜单权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17),
(1, 18), (1, 19), (1, 20);

-- 初始化测试楼盘数据
INSERT INTO house_building (building_name, building_code, province, city, district, address, avg_price, building_type, decoration_type, developer, property_company, green_rate, plot_ratio, total_units, description) VALUES
('星河湾花园', 'XHW2024001', '上海市', '上海市', '浦东新区', '张江高科技园区碧波路888号', 28000, '普通住宅', '精装修', '星河湾地产', '星河湾物业', 45, 2.5, 568, '星河湾花园位于浦东新区张江高科技园区核心位置，毗邻地铁2号线广兰路站，交通便利。项目总建筑面积约15万平方米，由8栋高层住宅组成。'),
('绿城玫瑰园', 'LCMM2024002', '上海市', '上海市', '徐汇区', '漕河泾开发区古美路1688号', 35000, '别墅', '精装修', '绿城集团', '绿城物业', 50, 1.2, 128, '绿城玫瑰园位于徐汇区漕河泾开发区，是绿城集团打造的高端别墅社区，容积率仅1.2，绿化率高达50%。'),
('恒大华府', 'HDHF2024003', '上海市', '上海市', '闵行区', '七宝镇漕宝路3333号', 22000, '普通住宅', '毛坯', '恒大集团', '金碧物业', 38, 3.0, 896, '恒大华府位于闵行区七宝板块，紧邻七宝古镇，配套成熟，生活便利。项目由12栋高层住宅组成，户型面积从80㎡到160㎡不等。');

-- 初始化测试户型数据
INSERT INTO house_unit (building_id, unit_name, unit_code, rooms, halls, bathrooms, area, price, orientation) VALUES
(1, 'A户型', 'XHW-A01', 2, 2, 1, 90, 252, '朝南'),
(1, 'B户型', 'XHW-B01', 3, 2, 2, 120, 336, '南北通透'),
(1, 'C户型', 'XHW-C01', 3, 2, 2, 140, 392, '朝南'),
(1, 'D户型', 'XHW-D01', 4, 2, 3, 180, 504, '南北通透'),
(2, '独栋别墅', 'LCMM-D01', 5, 3, 4, 350, 1225, '朝南'),
(2, '联排别墅', 'LCMM-L01', 4, 3, 3, 260, 910, '南北通透'),
(3, '两房两厅', 'HDHF-A01', 2, 2, 1, 85, 187, '朝南'),
(3, '三房两厅', 'HDHF-B01', 3, 2, 2, 115, 253, '南北通透');

-- 初始化测试直播数据
INSERT INTO live_session (session_name, session_code, building_id, live_type, anchor_id, anchor_name, start_time, end_time, status, introduction) VALUES
('星河湾花园专场直播', 'LIVE20240615001', 1, 0, 2, '王主播', '2024-06-15 14:00:00', '2024-06-15 15:30:00', 0, '带您走进星河湾花园，实地探访热销户型，了解最新优惠活动！'),
('绿城玫瑰园样板间揭秘', 'LIVE20240615002', 2, 0, 3, '李主播', '2024-06-15 19:30:00', '2024-06-15 21:00:00', 0, '独家揭秘绿城玫瑰园样板间，感受顶级豪宅的魅力！'),
('恒大华府优惠专场', 'LIVE20240616001', 3, 0, 2, '张主播', '2024-06-16 10:00:00', '2024-06-16 11:30:00', 0, '恒大华府限时优惠专场，最高优惠30万！');

-- 初始化测试客户数据
INSERT INTO customer_info (customer_name, customer_code, phone, gender, age, customer_source, customer_level, intention_status) VALUES
('张三', 'KH202406001', '13812345678', 1, 35, '1', 2, 2),
('李四', 'KH202406002', '13987654321', 1, 42, '2', 3, 3),
('王五', 'KH202406003', '13711112222', 2, 28, '1', 1, 1),
('赵六', 'KH202406004', '13633334444', 1, 50, '3', 3, 4),
('钱七', 'KH202406005', '13555556666', 2, 33, '4', 2, 2);

-- 初始化测试预约数据
INSERT INTO house_reserve (building_id, unit_id, reserve_code, customer_name, phone, visit_time, visit_type, visit_status) VALUES
(1, 1, 'YZ20240615001', '张三', '13812345678', '2024-06-16 10:00:00', 0, 1),
(2, 5, 'YZ20240615002', '李四', '13987654321', '2024-06-17 14:00:00', 0, 0),
(1, 2, 'YZ20240615003', '王五', '13711112222', '2024-06-18 09:00:00', 0, 1);

-- 初始化测试跟进记录数据
INSERT INTO customer_follow (customer_id, consultant_id, follow_type, follow_content, intention_level, next_follow_time) VALUES
(1, 2, 0, '电话联系客户，客户表示对90㎡户型感兴趣，约定本周六实地看房。', 2, '2024-06-16 18:00:00'),
(2, 2, 1, '微信发送楼盘资料和户型图，客户反馈良好，考虑购买别墅。', 3, '2024-06-17 10:00:00'),
(3, 3, 2, '短信发送最新优惠信息，客户回复会考虑。', 1, '2024-06-19 14:00:00');

-- H5 user remaining feature tables and compatibility upgrades
ALTER TABLE house_reserve ADD COLUMN app_user_id BIGINT COMMENT 'H5用户ID' AFTER id;
ALTER TABLE house_reserve ADD INDEX idx_app_user_id (app_user_id);

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

COMMIT;
