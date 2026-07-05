# 🥗 健康饮食推荐助手 (Healthy Diet Recommendation Assistant)

基于微信小程序的健康饮食管理与智能推荐系统，集成 AI 营养助手和菜品识别功能。

## 功能概览

| 模块 | 功能 |
|---|---|
| 🏠 首页 | 个性化饮食推荐、今日营养概览 |
| 📖 食谱浏览 | 食谱分类浏览、搜索筛选、营养详情 |
| ✍️ 食谱管理 | 用户上传食谱、管理员审核发布 |
| 📊 饮食记录 | 每日饮食打卡、分量记录 |
| 📈 营养统计 | 近7天营养趋势、三大营养素占比 |
| 🤖 AI 助手 | 基于讯飞星火大模型的营养问答 |
| 📷 菜品识别 | 基于百度 AI 的拍照识菜 |
| ❤️ 收藏 | 食谱收藏管理 |
| 🔐 用户系统 | 用户名密码登录 + 微信授权登录 |

## 技术栈

### 后端
- **框架**: Spring Boot 3.5.11
- **ORM**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0
- **安全**: Spring Security Crypto (BCrypt)
- **实时通信**: WebSocket (用于 AI 流式对话)
- **HTTP 客户端**: OkHttp 4.12
- **Java 版本**: JDK 17

### 前端
- **平台**: 微信小程序原生框架
- **基础库**: 3.14.2+

### 第三方服务
- 讯飞星火大模型 — AI 营养问答
- 百度 AI 菜品识别 — 拍照识别菜肴
- 微信开放平台 — 微信登录授权

## 项目结构

```
HealthyDietMiniprogram/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/cupk/healthy_diet/
│   │   ├── config/                   # 配置类（Spark、WeChat、Baidu 等）
│   │   ├── controller/               # REST 控制器
│   │   ├── dto/                      # 请求 DTO
│   │   ├── entity/                   # 数据库实体
│   │   ├── exception/                # 全局异常处理
│   │   ├── mapper/                   # MyBatis-Plus Mapper
│   │   ├── security/                 # 认证拦截 & Token 管理
│   │   ├── service/                  # 服务接口 & 实现
│   │   │   └── impl/                 # 推荐算法实现（协同过滤/基于内容/规则）
│   │   └── vo/                       # 响应 VO
│   └── src/main/resources/
│       ├── application-example.yaml  # 配置模板（复制为 application.yaml 使用）
│       └── healthy_diet_test.sql     # 数据库建表语句
│
├── miniprogram-frontend/             # 微信小程序前端
│   ├── pages/                        # 页面
│   │   ├── auth/                     # 登录/注册
│   │   ├── home/                     # 首页
│   │   ├── recipe/                   # 食谱（列表/详情/创建/管理/审核）
│   │   ├── record/                   # 饮食记录
│   │   ├── statistics/               # 营养统计
│   │   ├── health/                   # 健康信息
│   │   ├── recommend/                # 个性化推荐
│   │   ├── chat/                     # AI 营养助手
│   │   ├── dish-recognition/         # 拍照识菜
│   │   ├── favorites/                # 收藏
│   │   └── profile/                  # 个人中心
│   ├── components/                   # 公共组件
│   ├── utils/api.js                  # API 请求封装
│   └── project.config.json           # 小程序项目配置
│
├── .gitignore
└── README.md
```

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- 微信开发者工具
- （可选）讯飞星火 API 密钥
- （可选）百度 AI 应用凭证
- （可选）微信小程序 AppID & AppSecret

### 1. 数据库初始化

创建 MySQL 数据库并导入建表语句：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS healthy_diet_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p healthy_diet_test < backend/src/main/resources/healthy_diet_test.sql
```

### 2. 后端配置

```bash
cd backend
cp src/main/resources/application-example.yaml src/main/resources/application.yaml
```

编辑 `application.yaml`，根据实际情况填写配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/healthy_diet_test?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: <你的数据库用户名>
    password: <你的数据库密码>
```

> 所有敏感配置均支持环境变量注入（如 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 等），参考 `application-example.yaml` 中的占位符。

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

服务默认启动在 `http://localhost:8080`。

### 4. 配置微信小程序

1. 打开 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入项目 `miniprogram-frontend/` 目录
3. 在 `project.config.json` 中将 `appid` 改为你自己的小程序 AppID
4. 如果后端地址不是 `localhost:8080`，修改 `utils/api.js` 中的 `BASE_URL`

### 5. 可选：启用 AI 功能

<details>
<summary>讯飞星火 AI 问答</summary>

1. 前往 [讯飞开放平台](https://console.xfyun.cn/) 注册并创建应用
2. 获取 `appId`、`apiKey`、`apiSecret`
3. 在 `application.yaml` 中配置：

```yaml
spark:
  app-id: <你的 AppId>
  api-secret: <你的 ApiSecret>
  api-key: <你的 ApiKey>
```
</details>

<details>
<summary>百度菜品识别</summary>

1. 前往 [百度智能云](https://console.bce.baidu.com/) 创建应用，开通"菜品识别"接口
2. 获取 `API Key` 和 `Secret Key`
3. 在 `application.yaml` 中配置：

```yaml
baidu:
  dish:
    app-id: <你的 AppId>
    api-key: <你的 API Key>
    secret-key: <你的 Secret Key>
```
</details>

<details>
<summary>微信登录</summary>

1. 在 [微信公众平台](https://mp.weixin.qq.com/) 获取小程序的 `AppID` 和 `AppSecret`
2. 在 `application.yaml` 中配置：

```yaml
wechat:
  mini-program:
    app-id: <你的 AppID>
    app-secret: <你的 AppSecret>
```
</details>

## API 概览

| 路径 | 方法 | 说明 |
|---|---|---|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/wechat-login` | POST | 微信登录 |
| `/api/recipes` | GET | 食谱列表（支持分类/标签筛选） |
| `/api/recipes/{id}` | GET | 食谱详情 |
| `/api/recipes` | POST | 上传食谱（需登录） |
| `/api/health` | GET/POST | 查询/保存健康信息 |
| `/api/diet-records` | GET/POST | 查询/记录饮食 |
| `/api/recommendations/user/{id}` | GET | 个性化推荐 |
| `/api/statistics/{userId}` | GET | 营养统计 |
| `/api/favorites` | GET/POST/DELETE | 收藏管理 |
| `/api/ai/chat` | POST | AI 问答 |
| `/api/ai/chat/ws` | WebSocket | AI 流式对话 |
| `/api/dish/recognize` | POST | 菜品识别（上传图片） |
| `/api/file/upload` | POST | 文件上传 |

## 推荐算法

系统实现了三种推荐策略的混合：

1. **协同过滤**：基于用户行为相似度推荐
2. **基于内容**：根据用户健康目标、饮食偏好匹配食谱
3. **基于规则**：按健康目标（减脂/增肌/保持）直接推荐

三种策略的结果经过加权融合，结合用户忌口/过敏标签过滤后返回最终推荐列表。

## 许可证

本项目仅用于学习交流。
