# LiveHouse - 高端楼盘智慧看房&直播带看系统

> 本系统为高端楼盘专属智慧营销系统，聚焦别墅、大平层、顶奢住宅场景，整合线下VR智慧看房、线上云直播带看、客户精细化CRM运营能力。

## 🏗️ 项目架构

```
livehouse/
├── livehouse-api/          # 后端API服务 (Spring Boot)
├── livehouse-admin/        # 后台管理前端 (Vue3 + Element Plus)
└── livehouse-user/         # H5用户端前端 (Vue3)
```

## 🛠️ 技术栈

### 后端技术栈

| 组件 | 选型 | 版本 | 说明 |
|------|------|------|------|
| Java | 17 | - | 编程语言 |
| 核心框架 | Spring Boot | 2.7.x | 单体架构，前后端分离 |
| 持久层 | MyBatis-Plus | 3.5.x | 快速CRUD、分页、条件查询 |
| 数据库 | MySQL | 8.0 | 业务数据存储 |
| 缓存 | Redis | 6.x | 直播状态、在线人数、会话缓存 |
| 安全框架 | Spring Security + JWT | - | 登录认证、角色权限 |
| 直播服务 | 腾讯云直播SDK | - | 推拉流、录播、私密权限 |
| 文件存储 | 本地存储 + 阿里云OSS + 腾讯云COS | - | VR素材、视频存储 |
| 工具类 | Hutool | - | 工具集合 |

### 前端技术栈

| 组件 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 管理后台 | Vue3 + Vite | 3.x | 快速搭建后台管理页面 |
| UI框架 | Element Plus | - | 后台管理组件库 |
| 状态管理 | Pinia | - | Vue3状态管理 |
| VR组件 | Three.js | - | 720°VR全景漫游 |
| 直播播放器 | 腾讯云直播SDK | - | 直播推拉流播放 |
| 移动端 | H5 | - | 适配手机浏览器 |

## ✨ 核心功能

### 系统管理模块
- 用户管理 CRUD
- 角色管理 CRUD
- 菜单管理 CRUD
- 角色菜单分配
- JWT权限拦截器

### 房源管理模块
- 楼盘管理 CRUD
- 户型管理 CRUD
- VR素材管理 CRUD
- VR场景化配置
- 批量六面图合成工具

### 直播管理模块
- 直播场次 CRUD
- 白名单管理 CRUD/批量导入
- 开播/关播功能
- 三方直播平台配置
- 三方直播地址生成
- 直播封面图上传

### CRM客户管理模块
- 客户信息管理
- 客户跟进记录
- 预约看房管理

### 消息中心模块
- 消息模板管理
- 短信发送记录
- 系统消息推送

### H5用户端模块
- 首页展示（Banner、直播预告、楼盘推荐）
- 直播列表与详情
- VR全景看房
- 预约看房
- 收藏与浏览记录

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- npm 9+

### 后端启动

```bash
cd livehouse-api

# 1. 修改数据库配置
# 编辑 src/main/resources/application.yml
# 配置 MySQL 和 Redis 连接信息

# 2. 初始化数据库
# 执行 src/main/resources/sql/ 目录下的初始化SQL脚本

# 3. 编译运行
mvn clean package
mvn spring-boot:run
```

后端服务启动后访问：
- API接口：http://localhost:8080/api/
- Swagger文档：http://localhost:8080/swagger-ui.html

### 后台管理前端启动

```bash
cd livehouse-admin

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 构建生产版本
npm run build
```

后台管理页面访问：http://localhost:5173/

### H5用户端启动

```bash
cd livehouse-user

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 构建生产版本
npm run build
```

H5页面访问：http://localhost:5174/

## 📁 项目结构

### 后端结构

```
livehouse-api/src/main/java/com/livehouse/
├── common/              # 公共模块
│   ├── constants/       # 常量定义
│   ├── exception/       # 异常处理
│   └── result/          # 统一返回结果
├── config/              # 配置类
│   ├── SecurityConfig.java     # 安全配置
│   ├── WebMvcConfig.java       # Web配置
│   ├── MybatisPlusConfig.java  # MyBatis配置
│   ├── TencentLiveConfig.java  # 腾讯云直播配置
│   └── FileUploadProperties.java # 文件上传配置
├── controller/          # 控制器
│   ├── admin/           # 后台管理接口
│   ├── app/             # H5用户端接口
│   └── common/          # 公共接口
├── service/             # 服务层
│   ├── impl/            # 服务实现
│   └── file/            # 文件上传策略
├── mapper/              # 数据访问层
├── entity/              # 实体类
├── dto/                 # 数据传输对象
└── LiveHouseApplication.java  # 启动类
```

### 前端结构

```
livehouse-admin/src/
├── api/                 # API接口定义
├── components/          # 公共组件
├── views/               # 页面视图
│   ├── dashboard/       # 仪表盘
│   ├── system/          # 系统管理
│   ├── house/           # 房源管理
│   ├── live/            # 直播管理
│   ├── customer/        # 客户管理
│   └── message/         # 消息中心
├── router/              # 路由配置
├── stores/              # 状态管理
├── utils/               # 工具函数
└── main.js              # 入口文件
```

## 🔧 配置说明

### 腾讯云直播配置

在 `application.yml` 中配置腾讯云直播相关参数：

```yaml
tencent:
  live:
    secret-id: your-secret-id
    secret-key: your-secret-key
    push-domain: your-push-domain
    play-domain: your-play-domain
    app-name: live
    biz-id: your-biz-id
```

### 文件存储配置

支持本地存储、阿里云OSS、腾讯云COS三种存储方式：

```yaml
file:
  upload:
    strategy: local  # local / aliyun / tencent
    local:
      path: ./uploads
    aliyun:
      endpoint: your-endpoint
      access-key-id: your-key-id
      access-key-secret: your-key-secret
      bucket-name: your-bucket
```

## 📝 开发规范

- 后端使用 MyBatis-Plus Lambda 表达式进行条件查询，不使用原生SQL
- 前端使用 Vue3 Composition API
- 接口返回统一格式：`{ code, message, data }`
- 代码风格遵循阿里巴巴Java开发规范和Vue官方规范

## 📄 文档

- [项目开发设计文档](../document/项目开发设计文档.md)
- [项目开发进度表](../document/项目开发进度表.md)

## 📧 联系方式

如有问题，请联系项目开发团队。

---

**LiveHouse** - 让看房更智慧，让营销更精准