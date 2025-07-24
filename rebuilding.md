# SIAE项目模块化重构提示词

## 重构目标
将SIAE项目从当前的`siae-core` + `siae-common`双模块架构，重构为`siae-core` + `siae-web-starter` + `siae-security-starter`三层模块架构，实现更清晰的职责分离和按需装配。

## 重构步骤

### 第一步：扩展siae-core模块
```
请将siae-common模块中的以下基础组件迁移到siae-core模块：
1. 所有DTO类（如PageDTO、PageVO等）
2. 异常类（如BusinessException等）
3. 验证分组类（如CreateGroup、UpdateGroup）
4. 基础工具类（不依赖Spring Boot的工具类）
5. 基础枚举类

要求：
- 保持siae-core模块的轻量级特性，只依赖最基础的组件
- 不引入Spring Boot相关依赖
- 更新包结构为：com.hngy.siae.core.{dto,exception,validation,utils,enums}
- 保持向后兼容性，不破坏现有API
```

### 第二步：创建siae-web-starter模块
```
请创建新的siae-web-starter模块，包含以下组件：
1. 从siae-common迁移Web相关组件：
   - 统一响应处理（UnifiedResponseAdvice）
   - 全局异常处理（GlobalExceptionHandler）
   - Jackson配置
   - MybatisPlus配置
   - Web拦截器

2. 创建自动配置类：
   - WebAutoConfiguration：根据配置条件装配Web组件
   - WebProperties：Web相关配置属性类

3. 实现按需装配：
   - 使用@ConditionalOnProperty控制组件装配
   - 支持通过配置文件开关功能
   - 提供合理的默认配置

目录结构：
com.hngy.siae.web/
├── advice/            # 响应处理、异常处理
├── config/            # Web配置类
├── interceptor/       # Web拦截器
├── properties/        # 配置属性
└── autoconfigure/     # 自动配置
```

### 第三步：创建siae-security-starter模块
```
请创建新的siae-security-starter模块，包含以下组件：
1. 从siae-common迁移安全相关组件：
   - RedisPermissionService接口和实现
   - FallbackPermissionServiceImpl
   - OptimizedJwtAuthenticationFilter
   - 安全相关配置类

2. 实现智能装配机制：
   - 根据spring.application.name判断是否需要权限服务
   - 需要权限验证的服务：siae-auth, siae-user, siae-content, siae-admin
   - 其他服务默认不装配权限组件

3. 创建配置类：
   - SecurityAutoConfiguration：安全组件自动配置
   - SecurityProperties：安全相关配置属性

4. 支持优雅降级：
   - Redis不可用时自动切换到Fallback实现
   - 提供配置开关控制各项安全功能

目录结构：
com.hngy.siae.security/
├── service/           # 权限服务
├── filter/            # 安全过滤器
├── config/            # 安全配置
├── properties/        # 配置属性
└── autoconfigure/     # 自动配置
```

### 第四步：更新服务依赖
```
请更新各个业务服务的pom.xml依赖：

1. 需要完整功能的服务（siae-user, siae-content, siae-auth）：
   - 依赖：siae-core + siae-web-starter + siae-security-starter

2. 轻量级服务（siae-message等）：
   - 依赖：siae-core + siae-web-starter
   - 不引入siae-security-starter，避免不必要的权限组件

3. 网关服务：
   - 根据实际需求选择依赖模块
```

### 第五步：配置文件优化
```
请为各个starter模块创建配置属性类和默认配置：

1. WebProperties配置类：
   - 统一响应开关
   - Jackson序列化配置
   - Web组件开关

2. SecurityProperties配置类：
   - JWT配置
   - 权限缓存配置
   - 安全功能开关

3. 各服务的application.yml示例配置：
   - 展示如何通过配置控制功能开关
   - 提供合理的默认值
```

### 第六步：自动配置文件
```
请为新创建的starter模块添加Spring Boot自动配置：

1. 创建META-INF/spring.factories文件
2. 注册自动配置类
3. 确保自动配置的条件注解正确
4. 测试自动配置是否生效
```

## 重构要求

### 代码质量要求
- 保持现有API的向后兼容性
- 添加详细的JavaDoc注释
- 使用合适的条件注解控制Bean装配
- 提供合理的默认配置值

### 配置要求
- 所有功能都应该支持配置开关
- 提供清晰的配置属性文档
- 配置属性应该有合理的默认值
- 支持不同环境的配置差异

### 测试要求
- 为自动配置类编写单元测试
- 测试条件装配是否正确工作
- 验证配置属性是否生效
- 确保重构后功能完整性

## 验证标准

重构完成后，请验证以下功能：
1. 各个服务能够正常启动
2. 权限验证功能正常工作
3. 不需要权限的服务没有装配权限组件
4. 配置开关能够正确控制功能
5. Redis不可用时能够优雅降级

## 注意事项
- 重构过程中保持Git提交的原子性
- 每个步骤完成后进行功能验证
- 保留原有的siae-common模块作为过渡，待重构完成后再删除
- 注意处理循环依赖问题
- 确保所有import语句正确更新
