# 通信服务架构重构实施方案

## 目标架构

```
siae-notification    # 通知服务（MVC）- 邮件/短信/站内通知
siae-communication   # 实时通信（Netty+MVC）- 聊天/在线状态/WebSocket
siae-live           # 直播课堂（MVC+流媒体）- 视频直播/互动白板
```

## Phase 1: 重构notification服务

### 1.1 重命名服务
```bash
mv services/siae-message services/siae-notification
```

### 1.2 修改配置
- `pom.xml`: artifactId改为`siae-notification`
- `application-dev.yaml`: spring.application.name改为`siae-notification`
- `bootstrap.yaml`: spring.application.name改为`siae-notification`

### 1.3 添加数据库支持
```xml
<!-- pom.xml添加 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

移除`@SpringBootApplication`中的`exclude = {DataSourceAutoConfiguration.class}`

### 1.4 创建数据库表
```sql -- 改成notification_db
-- message_db.sql
CREATE DATABASE IF NOT EXISTS message_db CHARACTER SET utf8mb4;
USE message_db;

-- 站内通知表
CREATE TABLE system_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
) COMMENT='系统通知表';

-- 邮件发送记录表
CREATE TABLE email_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient VARCHAR(200) NOT NULL COMMENT '收件人',
    subject VARCHAR(500) COMMENT '主题',
    status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    error_msg TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) COMMENT='邮件发送记录';
```

### 1.5 实现通知功能
创建以下文件：
- `entity/SystemNotification.java`
- `mapper/SystemNotificationMapper.java`
- `service/NotificationService.java`
- `service/impl/NotificationServiceImpl.java`
- `controller/NotificationController.java`
- `dto/request/NotificationCreateDTO.java`
- `dto/response/NotificationVO.java`

### 1.6 更新Nacos配置

[//]: # (端口：8030 → 8040（避免与未来服务冲突）)
不改端口号
---

## Phase 2: 创建communication服务

### 2.1 创建模块结构
```bash
mkdir -p services/siae-communication/src/main/java/com/hngy/siae/communication
mkdir -p services/siae-communication/src/main/resources
```

### 2.2 pom.xml
```xml
<artifactId>siae-communication</artifactId>
<dependencies>
    <!-- 内部依赖 -->
    <dependency>
        <groupId>com.hngy</groupId>
        <artifactId>siae-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.hngy</groupId>
        <artifactId>siae-web-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.hngy</groupId>
        <artifactId>siae-security-starter</artifactId>
    </dependency>

    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Netty -->
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.100.Final</version>
    </dependency>

    <!-- 数据库 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Nacos -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>
</dependencies>
```

### 2.3 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS communication_db CHARACTER SET utf8mb4;
USE communication_db;

-- 会话表
CREATE TABLE chat_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type ENUM('PRIVATE', 'GROUP') NOT NULL COMMENT '会话类型：私聊/群聊',
    name VARCHAR(100) COMMENT '会话名称（群聊用）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='聊天会话表';

-- 会话成员表
CREATE TABLE chat_conversation_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(50) COMMENT '成员昵称',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_conversation_user (conversation_id, user_id),
    INDEX idx_user (user_id)
) COMMENT='会话成员表';

-- 消息表
CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type ENUM('TEXT', 'IMAGE', 'FILE', 'AUDIO', 'VIDEO') DEFAULT 'TEXT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation_time (conversation_id, created_at),
    INDEX idx_sender (sender_id)
) COMMENT='聊天消息表';

-- 在线状态表（Redis为主，MySQL备份）
CREATE TABLE online_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    server_address VARCHAR(100) COMMENT '服务器地址',
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) COMMENT='用户在线状态';
```

### 2.4 核心代码结构
```
com.hngy.siae.communication/
├── SiaeCommunicationApplication.java
├── netty/
│   ├── NettyWebSocketServer.java           # Netty服务器启动类
│   ├── WebSocketServerInitializer.java     # Channel初始化
│   ├── WebSocketFrameHandler.java          # 消息处理器
│   └── ChannelManager.java                 # 连接管理器
├── controller/
│   ├── ChatController.java                 # 聊天REST API
│   ├── ConversationController.java         # 会话管理API
│   └── OnlineStatusController.java         # 在线状态API
├── service/
│   ├── ChatService.java
│   ├── ConversationService.java
│   ├── MessagePushService.java
│   └── OnlineStatusService.java
├── entity/
│   ├── ChatConversation.java
│   ├── ChatMessage.java
│   └── ConversationMember.java
└── config/
    └── NettyServerConfig.java
```

### 2.5 配置文件
```yaml
# application-dev.yaml
server:
  port: 8050

spring:
  application:
    name: siae-communication
  datasource:
    url: jdbc:mysql://localhost:3306/communication_db
    username: root
    password: 123456

# Netty WebSocket端口
netty:
  websocket:
    port: 9090
    boss-threads: 1
    worker-threads: 8
```

### 2.6 启动类
```java
@SpringBootApplication(scanBasePackages = "com.hngy.siae")
@EnableFeignClients(basePackages = "com.hngy.siae")
public class SiaeCommunicationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiaeCommunicationApplication.class, args);
    }
}
```

---

## Phase 3: 创建live服务（后续）

### 3.1 模块结构
```
services/siae-live/
├── pom.xml
├── src/main/java/com/hngy/siae/live/
│   ├── SiaeLiveApplication.java
│   ├── controller/LiveRoomController.java
│   ├── service/LiveStreamService.java
│   └── webrtc/SignalingController.java
└── src/main/resources/
    ├── application-dev.yaml
    └── sql/live_db.sql
```

### 3.2 数据库
```sql
CREATE DATABASE IF NOT EXISTS live_db CHARACTER SET utf8mb4;
USE live_db;

-- 直播间表
CREATE TABLE live_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    teacher_id BIGINT NOT NULL COMMENT '主讲教师ID',
    status ENUM('PENDING', 'LIVE', 'ENDED') DEFAULT 'PENDING',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT='直播间表';

-- 参与者表
CREATE TABLE live_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('TEACHER', 'STUDENT') DEFAULT 'STUDENT',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_user (room_id, user_id)
) COMMENT='直播参与者';
```

### 3.3 配置
```yaml
server:
  port: 8060
spring:
  application:
    name: siae-live

# 流媒体配置
stream:
  rtmp-url: rtmp://localhost:1935/live
  hls-url: http://localhost:8080/hls
```

---

## 服务间调用规则

### notification服务对外接口
```java
// 提供Feign Client（安全，无架构冲突）
@FeignClient(name = "siae-notification")
public interface NotificationFeignClient {
    @PostMapping("/api/v1/notification/send")
    Result<?> sendNotification(@RequestBody NotificationDTO dto);
}
```

### communication服务对外接口
```java
// ❌ 不提供Feign Client
// ✅ 只提供HTTP REST API
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @PostMapping("/system/send")
    public Result<?> sendSystemMessage(@RequestBody SystemMessageDTO dto) {
        // 发送系统消息
    }

    @GetMapping("/history/{conversationId}")
    public Result<List<ChatMessageVO>> getHistory(@PathVariable Long conversationId) {
        // 查询聊天历史
    }
}
```

### 其他服务调用communication
```java
// siae-core中创建轻量级客户端
@Component
public class CommunicationApiClient {
    @Autowired
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://siae-communication:8050";

    public void sendSystemMessage(Long userId, String content) {
        restTemplate.postForObject(
            BASE_URL + "/api/v1/chat/system/send",
            Map.of("userId", userId, "content", content),
            Result.class
        );
    }
}
```

---

## 实施顺序

1. ✅ Phase 1.1-1.3: 重命名message服务为notification
2. ✅ Phase 1.4: 创建message_db数据库
3. ✅ Phase 1.5: 实现站内通知功能
4. ✅ Phase 2.1-2.3: 创建communication服务骨架
5. ✅ Phase 2.4-2.6: 实现Netty WebSocket功能
6. ✅ Phase 2.5: 实现聊天REST API
7. ⏸️ Phase 3: 创建live服务（需求明确后）

---

## 注意事项

1. **端口规划**
   - notification: 8040 (HTTP)
   - communication: 8050 (HTTP) + 9090 (WebSocket)
   - live: 8060 (HTTP)

2. **依赖原则**
   - notification可被Feign调用
   - communication只提供HTTP API
   - live可调用communication的HTTP API

3. **数据库分离**
   - message_db（通知）
   - communication_db（聊天）
   - live_db（直播）

4. **Redis使用**
   - 在线状态：`online:user:{userId}`
   - 离线消息：`offline:msg:{userId}`
   - 分布式推送：`chat:push:{userId}`