### 网关的配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: siae-auth-route
          uri: lb://siae-auth
          predicates: # 断言工厂
            - Path=/api/v1/auth/**
          filter:
          order:
          metadata:
            hello:world
      default-filter: # 默认过滤器，会对所有routes添加
        -AddResponseHeader=X-Response-Abc, 123
```

1. 再route配置中，是从上往下匹配请求，如果想要改变顺序，可以设置order
2. route的断言（predicates）长写法，以及其他的断言工厂
3. 自定义断言工厂，满足条件才跳转
4. 过滤器工厂，路径重写工厂可能会用到
5. 全局过滤器，感觉可以用来做一些信息统计
6. 自定义过滤器工厂
    + 方式一：继承 AbstractGatewayFilterFactory（局部过滤器），需要在路由中配置
    + 方式二：实现GlobalFilter（全局过滤器），自动应用到所有路由
7. 跨域配置

### Security
@PreAuthorize("hasRole('ADMIN') or hasAuthority('user:edit')")