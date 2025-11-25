# 多阶段构建 Dockerfile for siae-media service
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /build

# 复制父 POM 和依赖配置
COPY ../../pom.xml ./pom.xml
COPY ../../package/siae-core ./siae-core
COPY ../../siae-starters ./siae-starters

# 复制服务代码
COPY . ./services/siae-media

# 构建项目（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -pl services/siae-media -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# 安装必要的工具
RUN apk add --no-cache \
    curl \
    tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

# 创建应用用户（安全最佳实践）
RUN addgroup -g 1000 siae && \
    adduser -D -u 1000 -G siae siae

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/services/siae-media/target/*.jar app.jar

# 修改文件所有者
RUN chown -R siae:siae /app

# 切换到非 root 用户
USER siae

# 暴露端口
EXPOSE 8084

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
