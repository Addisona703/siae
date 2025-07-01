# 认证服务接口文档 (Auth Service API)

## 1. 认证接口

### 1.1 用户登录
- **接口**：`POST /api/v1/auth/login`
- **请求参数**：
```json
{
    "username": "string",
    "password": "string"
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": "long",
        "username": "string",
        "accessToken": "string",
        "refreshToken": "string",
        "tokenType": "string",
        "expiresIn": "long"
    }
}
```

### 1.2 刷新令牌
- **接口**：`POST /api/v1/auth/token/refresh`
- **请求参数**：
```json
{
    "refreshToken": "string"
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "accessToken": "string",
        "refreshToken": "string",
        "tokenType": "string",
        "expiresIn": "long"
    }
}
```

### 1.3 注销登录
- **接口**：`POST /api/v1/auth/logout`
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

## 2. 权限管理接口

### 2.1 角色管理

#### 2.1.1 创建角色
- **接口**：`POST /api/v1/auth/roles`
- **请求参数**：
```json
{
    "name": "string",
    "code": "string",
    "description": "string"
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "roleId": "long",
        "name": "string",
        "code": "string",
        "description": "string",
        "status": "int"
    }
}
```

#### 2.1.2 修改角色
- **接口**：`PUT /api/v1/auth/roles/{roleId}`
- **请求参数**：
```json
{
    "name": "string",
    "description": "string",
    "status": "int"
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 2.2 权限配置

#### 2.2.0 新增权限（仅超级管理员）
- **接口**：`POST /api/v1/auth/permissions`
- **请求参数**：
```json
{
    "name": "string",
    "code": "string",
    "type": "string",
    "parentId": "long",
    "path": "string",
    "component": "string",
    "icon": "string",
    "sortOrder": "int"
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": "long",
        "name": "string",
        "code": "string",
        "type": "string",
        "parentId": "long",
        "path": "string",
        "component": "string",
        "icon": "string",
        "sortOrder": "int",
        "status": "int"
    }
}
```

#### 2.2.1 获取权限列表
- **接口**：`GET /api/v1/auth/permissions`
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": [{
        "id": "long",
        "name": "string",
        "code": "string",
        "type": "string",
        "parentId": "long",
        "path": "string",
        "component": "string",
        "icon": "string",
        "sortOrder": "int",
        "status": "int"
    }]
}
```

#### 2.2.2 角色权限分配
- **接口**：`POST /api/v1/auth/roles/{roleId}/permissions`
- **请求参数**：
```json
{
    "permissionIds": ["long"]
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 2.3 用户角色管理

#### 2.3.1 分配用户角色
- **接口**：`POST /api/v1/auth/users/{userId}/roles`
- **请求参数**：
```json
{
    "roleIds": ["long"]
}
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

## 3. 日志管理接口

### 3.1 查询登录日志
- **接口**：`GET /api/v1/auth/logs/login`
- **请求参数**：
```
startTime: datetime (查询开始时间)
endTime: datetime (查询结束时间)
username: string (可选，用户名)
status: int (可选，登录状态)
page: int (页码)
size: int (每页记录数)
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",
        "list": [{
            "id": "long",
            "userId": "long",
            "username": "string",
            "loginIp": "string",
            "loginLocation": "string",
            "browser": "string",
            "os": "string",
            "status": "int",
            "msg": "string",
            "loginTime": "datetime"
        }]
    }
}
```

### 3.2 查询登录失败记录
- **接口**：`GET /api/v1/auth/logs/login-fail`
- **请求参数**：
```
startTime: datetime (查询开始时间)
endTime: datetime (查询结束时间)
username: string (可选，用户名)
loginIp: string (可选，登录IP)
page: int (页码)
size: int (每页记录数)
```
- **响应结果**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": "long",
        "list": [{
            "id": "long",
            "userId": "long",
            "username": "string",
            "loginIp": "string",
            "failReason": "string",
            "failTime": "datetime"
        }]
    }
}
```