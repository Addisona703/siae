# SIAE Attendance Service (考勤服务)

## 概述

考勤服务是 SIAE 系统的核心业务模块，负责管理组织成员的考勤记录、请假申请和审核流程。

## 核心功能

- **签到签退管理** - 支持位置验证、时间窗口检查、重复签到防护
- **考勤记录管理** - 查询、统计、导出功能
- **考勤异常检测** - 自动识别迟到、早退、缺勤、漏打卡
- **请假申请管理** - 支持病假和事假两种类型、日期冲突检测
- **请假审核流程** - 审批流程、通知机制
- **请假与考勤联动** - 自动处理请假期间的考勤异常
- **考勤规则配置** - 灵活的规则管理系统
- **考勤报表统计** - 统计分析和数据导出

## 技术栈

- **框架**: Spring Boot 3.2.5
- **ORM**: MyBatis Plus 3.5.6
- **数据库**: MySQL 8.0+
- **缓存**: Redis (用于防重复签到)
- **消息队列**: RabbitMQ (用于异步通知)
- **API文档**: SpringDoc OpenAPI 3
- **工具库**: Hutool, Lombok

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 启动步骤

1. 创建数据库
```sql
CREATE DATABASE attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置数据库连接
编辑 `src/main/resources/application-dev.yaml`，修改数据库和 Redis 连接信息。

3. 启动服务
```bash
mvn spring-boot:run
```

4. 访问 API 文档
```
http://localhost:8050/api/v1/attendance/swagger-ui.html
```

## 服务端口

- 开发环境: 8050
- 服务路径: `/api/v1/attendance`

## 主要接口

### 考勤管理
- `POST /check-in` - 签到
- `POST /check-out` - 签退
- `GET /records/{id}` - 查询考勤记录
- `POST /records/page` - 分页查询考勤记录
- `GET /records/export` - 导出考勤记录

### 请假管理
- `POST /leaves` - 提交请假申请
- `POST /leaves/{id}/approve` - 审核请假申请
- `GET /leaves/{id}` - 查询请假申请
- `GET /leaves/pending` - 查询待审核请假列表

### 考勤异常
- `GET /anomalies/{id}` - 查询考勤异常
- `POST /anomalies/page` - 分页查询考勤异常
- `POST /anomalies/{id}/handle` - 处理考勤异常

### 考勤规则
- `POST /rules` - 创建考勤规则
- `PUT /rules/{id}` - 更新考勤规则
- `GET /rules` - 查询规则列表

### 统计报表
- `GET /statistics/personal/{userId}` - 查询个人考勤统计
- `GET /statistics/department/{departmentId}` - 查询部门考勤统计
- `POST /statistics/report` - 生成考勤报表

## 开发文档

详细的设计文档和需求文档请参考：
- [需求文档](/.kiro/specs/attendance-service/requirements.md)
- [设计文档](/.kiro/specs/attendance-service/design.md)
- [任务列表](/.kiro/specs/attendance-service/tasks.md)

## 许可证

Copyright © 2024 SIAE Team
