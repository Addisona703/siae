1. 注册流程
   前端提交注册信息给 auth 服务

auth 调用 user-api 的用户创建接口，写入用户基本信息到数据库

注册时可能需要验证码，调用 message 发送验证码（短信/邮件）

2. 验证码发送和校验
   验证码的发送逻辑在 message，封装发送邮件、短信的具体实现

auth 调用 message 接口，触发验证码发送

验证码一般缓存到 Redis（message 负责写入缓存）

验证码的校验主要在 auth，通过调用 message 相关接口（或者直接调用 Redis）比对用户提交的验证码是否正确

3. 登录流程
   用户提交手机号+验证码给 auth

auth 调用 message 验证码校验接口（或者自己去 Redis 查）

校验成功后，auth 调用 user-api 查询用户信息，生成 JWT 返回给前端

4. 认证和鉴权
   前端持有 JWT，调用各微服务接口时携带

gateway 或各服务进行 JWT 校验，识别用户身份

user 模块可提供权限信息接口，或直接做权限校验
```text
[前端]
   ↓ 注册表单
[siae-auth] -------------------
   ↓ 创建用户              ↓ 发送验证码
[siae-user-api]         [siae-message]
                            ↓ 缓存验证码
[前端]
   ↓ 输入验证码 + 登录请求
[siae-auth]
   ↓ 验证码校验 <------- [siae-message（Redis缓存）]
   ↓ 查询用户信息 ------> [siae-user-api]
   ↓ 返回 JWT
[前端] 持 JWT 调用其他服务
```