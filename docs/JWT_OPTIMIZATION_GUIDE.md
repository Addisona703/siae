# JWT认证系统优化指南

## 概述

本文档描述了SIAE项目中JWT认证系统的优化实现，主要解决了JWT token过大导致的数据库存储错误问题，并通过Redis缓存提升了权限查询性能。

## 优化前后对比

### 优化前的问题
- **JWT Token过大**: Token中包含完整的权限列表，导致token长度过长
- **数据库存储错误**: 长token无法存储到数据库字段中
- **权限更新延迟**: 权限变更需要重新登录才能生效
- **性能问题**: 每次请求都需要解析大量权限信息

### 优化后的优势
- **Token大小减少**: JWT只包含基本信息（userId, username, exp）
- **实时权限更新**: 权限信息存储在Redis中，可以实时更新
- **高性能**: Redis缓存提供毫秒级权限查询
- **优雅降级**: Redis不可用时自动回退到传统模式

## 架构设计

### 核心组件

1. **OptimizedJwtAuthenticationFilter**: 优化的JWT认证过滤器
2. **RedisPermissionCacheService**: Redis权限缓存服务
3. **RedisPermissionService**: 通用权限查询接口
4. **JwtUtils**: 优化的JWT工具类

### 数据流程

```
用户登录 → 生成简化JWT → 权限存储到Redis → 后续请求从Redis获取权限
```

## 使用指南

### 1. 登录流程

```java
// 优化后的登录代码
@Override
public LoginVO login(LoginDTO loginDTO, String clientIp, String browser, String os) {
    // 1. 验证用户凭据
    UserVO user = userClient.getUserByUsername(loginDTO.getUsername());
    
    // 2. 查询用户权限
    List<String> permissions = permissionMapper.selectByUserId(user.getId())
            .stream()
            .map(Permission::getCode)
            .collect(Collectors.toList());
    
    // 3. 生成优化的JWT（不包含权限）
    String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername());
    
    // 4. 将权限缓存到Redis
    long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
    redisPermissionCacheService.cacheUserPermissions(user.getId(), permissions, tokenExpireSeconds, TimeUnit.SECONDS);
    
    return loginVO;
}
```

### 2. 权限验证

```java
// 控制器中使用@PreAuthorize注解
@GetMapping("/create")
@PreAuthorize("hasAuthority('CONTENT_CREATE')")
public Result<ContentVO> createContent(@RequestBody ContentDTO contentDTO) {
    // 业务逻辑
}

// 或使用@Secured注解
@GetMapping("/admin")
@Secured("ROLE_ADMIN")
public Result<String> adminOnly() {
    // 管理员专用功能
}
```

### 3. 获取当前用户信息

```java
// 在业务代码中获取当前用户信息
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Long userId = (Long) auth.getDetails();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
```

## 配置说明

### Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # 设置Redis密码（如果有）
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

### 安全配置

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final OptimizedJwtAuthenticationFilter optimizedJwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/register").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(optimizedJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## 测试验证

### 1. 基本认证测试

```bash
# 登录获取token
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# 使用token访问受保护资源
curl -X GET http://localhost:8010/api/v1/content/auth-test/basic \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. 权限测试

```bash
# 测试特定权限
curl -X GET http://localhost:8010/api/v1/content/auth-test/content-create \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 测试管理员角色
curl -X GET http://localhost:8010/api/v1/content/auth-test/admin-role \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 错误处理

### Redis连接失败

当Redis不可用时，系统会自动回退到以下策略：

1. **FallbackPermissionServiceImpl**: 返回空权限列表
2. **FallbackSecurityConfig**: 使用传统JWT认证
3. **日志记录**: 记录Redis连接失败信息

### 常见错误及解决方案

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| 403 Forbidden | 权限不足 | 检查用户权限配置 |
| 401 Unauthorized | Token无效 | 重新登录获取新token |
| Redis连接失败 | Redis服务不可用 | 检查Redis服务状态 |

## 性能监控

### Redis缓存监控

```bash
# 查看权限缓存
redis-cli keys "auth:perms:*"
redis-cli keys "auth:roles:*"

# 查看缓存过期时间
redis-cli ttl "auth:perms:1"
```

### 日志监控

```java
# 关键日志级别设置
logging:
  level:
    com.hngy.siae.common.filter.OptimizedJwtAuthenticationFilter: DEBUG
    com.hngy.siae.auth.service.impl.RedisPermissionCacheServiceImpl: DEBUG
```

## 最佳实践

1. **权限粒度**: 合理设计权限粒度，避免权限过多导致缓存膨胀
2. **缓存过期**: 权限缓存TTL应与JWT过期时间保持一致
3. **错误处理**: 优雅处理Redis连接失败，不影响主业务流程
4. **监控告警**: 监控Redis连接状态和权限缓存命中率
5. **安全考虑**: 定期清理过期的权限缓存，防止内存泄漏

## 升级指南

### 从旧版本升级

1. **添加Redis依赖**: 在需要的服务中添加Redis starter
2. **更新配置**: 添加Redis连接配置
3. **重新部署**: 按服务依赖顺序重新部署
4. **验证功能**: 使用测试接口验证权限功能

### 兼容性说明

- ✅ 向后兼容：旧版本JWT token仍然可以正常使用
- ✅ 渐进升级：可以逐个服务进行升级
- ✅ 回滚支持：可以快速回滚到旧版本

## 故障排查

### 常用排查命令

```bash
# 检查Redis连接
redis-cli ping

# 查看用户权限缓存
redis-cli get "auth:perms:USER_ID"

# 查看JWT token内容
echo "JWT_TOKEN" | cut -d'.' -f2 | base64 -d | jq
```

### 日志分析

关注以下关键日志：
- JWT token验证失败
- Redis连接异常
- 权限验证失败
- 用户认证成功/失败

---

**注意**: 本优化方案已在开发环境中验证，生产环境部署前请进行充分测试。
