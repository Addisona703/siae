# SiaeAuthorize注解测试指南

## 测试接口列表

### 1. 基础功能测试

#### 1.1 公开接口（无权限要求）
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/public" \
  -H "Content-Type: application/json"
```
**期望结果**: 成功返回，无需认证

#### 1.2 获取当前用户信息
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/current-user" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 返回当前用户的详细信息，包括权限列表

### 2. 权限控制测试

#### 2.1 超级管理员测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/super-admin" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 
- 如果是超级管理员（ROLE_ROOT），应该成功访问
- 如果不是超级管理员，应该返回权限不足异常

#### 2.2 具体权限测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/auth-log-query" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 
- 有AUTH_LOG_QUERY权限或超级管理员：成功
- 无权限：返回权限不足异常

#### 2.3 角色测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/admin-role" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 
- 有ROLE_ADMIN角色或超级管理员：成功
- 无角色：返回权限不足异常

### 3. 复合权限表达式测试

#### 3.1 OR逻辑测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/or-expression" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 有ROLE_ADMIN角色或AUTH_LOG_QUERY权限或超级管理员即可访问

#### 3.2 AND逻辑测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/and-expression" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 必须同时有ROLE_ADMIN角色和AUTH_LOG_QUERY权限，或者是超级管理员

#### 3.3 复杂表达式测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/complex-expression" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: (ROLE_ADMIN && AUTH_LOG_QUERY) || ROLE_SUPER_ADMIN || 超级管理员

### 4. 异常情况测试

#### 4.1 权限不足测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/permission-denied" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 返回权限不足异常（除非是超级管理员）

#### 4.2 空表达式测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/empty-expression" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 返回未授权访问异常（除非是超级管理员）

#### 4.3 未认证测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/auth-log-query" \
  -H "Content-Type: application/json"
```
**期望结果**: 返回用户未认证异常

### 5. 性能测试

#### 5.1 权限检查性能测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/performance-test" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 返回执行时间信息

#### 5.2 批量权限测试
```bash
curl -X GET "http://localhost:8000/test/siae-authorize/batch-test" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```
**期望结果**: 返回当前用户的各种权限检查结果

### 6. POST请求测试

#### 6.1 POST权限测试
```bash
curl -X POST "http://localhost:8000/test/siae-authorize/post-test" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'
```
**期望结果**: 有权限时成功，无权限时返回异常

### 7. 原始LogController测试

#### 7.1 登录日志查询测试
```bash
curl -X POST "http://localhost:8000/logs/login" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10,
    "data": {}
  }'
```

#### 7.2 登录失败日志查询测试
```bash
curl -X POST "http://localhost:8000/logs/login/fail" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10,
    "data": {}
  }'
```

## 测试步骤

### 步骤1: 启动服务
```bash
cd services/siae-auth
mvn spring-boot:run
```

### 步骤2: 获取JWT Token
首先需要登录获取JWT Token：
```bash
curl -X POST "http://localhost:8000/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password"
  }'
```

### 步骤3: 使用Token测试接口
将获取到的token替换到上面的测试命令中的`YOUR_JWT_TOKEN`部分。

### 步骤4: 验证日志输出
查看控制台日志，应该能看到：
- `SiaeAuthorizeAspect`的权限检查日志
- 超级管理员放行的日志
- 权限表达式评估的日志
- 权限不足时的警告日志

## 预期的日志输出示例

### 成功访问（普通用户）
```
DEBUG - 评估权限表达式: hasAuthority('auth:log:query')
DEBUG - 权限校验通过
```

### 超级管理员访问
```
DEBUG - 超级管理员访问，直接放行
```

### 权限不足
```
WARN - 用户 testuser 权限不足，表达式: hasAuthority('NON_EXISTENT_PERMISSION')
```

### 未认证访问
```
WARN - 用户未认证，拒绝访问
```

## 故障排查

### 1. 如果切面不生效
检查：
- `SiaeAuthorizeAspect`是否被Spring容器管理
- `@EnableAspectJAutoProxy`是否启用
- 依赖中是否包含`spring-boot-starter-aop`

### 2. 如果权限检查不正确
检查：
- JWT Token是否有效
- 用户权限数据是否正确
- SpEL表达式语法是否正确

### 3. 如果超级管理员不生效
检查：
- 用户是否真的拥有`ROLE_ROOT`角色
- 角色名称是否完全匹配（区分大小写）

## 测试结果记录

请记录每个测试接口的结果：

| 接口 | 超级管理员 | 有权限用户 | 无权限用户 | 未认证用户 |
|------|------------|------------|------------|------------|
| /public | ✅ | ✅ | ✅ | ✅ |
| /super-admin | ✅ | ❌ | ❌ | ❌ |
| /auth-log-query | ✅ | ✅ | ❌ | ❌ |
| /admin-role | ✅ | ✅ | ❌ | ❌ |
| /or-expression | ✅ | ✅ | ❌ | ❌ |
| /and-expression | ✅ | ✅ | ❌ | ❌ |
| /permission-denied | ✅ | ❌ | ❌ | ❌ |
| /empty-expression | ✅ | ❌ | ❌ | ❌ |

✅ = 应该成功访问
❌ = 应该返回异常
