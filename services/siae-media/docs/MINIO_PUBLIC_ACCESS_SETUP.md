# MinIO 公开访问配置指南

## 概述

为了支持公开文件的永久 URL 访问（不需要签名），需要配置 MinIO 桶策略，允许匿名访问 `public` 路径下的文件。

## 配置步骤

### 方式一：使用 MinIO Console（推荐）

1. **访问 MinIO Console**
   ```
   http://localhost:9001
   ```
   使用管理员账号登录（默认：root / 123456789）

2. **选择存储桶**
   - 点击左侧菜单 "Buckets"
   - 选择 `siae-media` 桶

3. **配置访问策略**
   - 点击 "Access" 标签
   - 点击 "Add Access Rule" 按钮
   - 配置如下：
     - **Prefix**: `*/public/*`
     - **Access**: `readonly` (只读)
   - 点击 "Save" 保存

### 方式二：使用 mc 命令行工具

1. **安装 mc 工具**
   ```bash
   # Windows
   wget https://dl.min.io/client/mc/release/windows-amd64/mc.exe
   
   # Linux
   wget https://dl.min.io/client/mc/release/linux-amd64/mc
   chmod +x mc
   
   # macOS
   brew install minio/stable/mc
   ```

2. **配置 MinIO 连接**
   ```bash
   mc alias set local http://localhost:9000 root 123456789
   ```

3. **设置桶策略**
   ```bash
   # 创建策略文件 public-policy.json
   cat > public-policy.json << 'EOF'
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Principal": {"AWS": ["*"]},
         "Action": ["s3:GetObject"],
         "Resource": ["arn:aws:s3:::siae-media/*/public/*"]
       }
     ]
   }
   EOF
   
   # 应用策略
   mc anonymous set-json public-policy.json local/siae-media
   ```

4. **验证策略**
   ```bash
   mc anonymous get local/siae-media
   ```

### 方式三：使用 MinIO Java SDK（程序化配置）

在 `MinioInitializer` 中添加自动配置：

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MinioInitializer implements ApplicationRunner {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String bucketName = minioConfig.getBucketName();
        
        // 1. 确保桶存在
        ensureBucketExists(bucketName);
        
        // 2. 配置公开访问策略
        configurePublicAccessPolicy(bucketName);
    }

    private void ensureBucketExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );
        
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("Created bucket: {}", bucketName);
        }
    }

    private void configurePublicAccessPolicy(String bucketName) throws Exception {
        String policy = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*/public/*"]
                }
              ]
            }
            """.formatted(bucketName);
        
        minioClient.setBucketPolicy(
            SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policy)
                .build()
        );
        
        log.info("Configured public access policy for bucket: {}", bucketName);
    }
}
```

## 验证配置

### 1. 上传测试文件

```bash
# 使用 mc 上传测试文件到 public 路径
mc cp test.jpg local/siae-media/tenant-001/public/test.jpg
```

### 2. 测试公开访问

```bash
# 不需要签名，直接访问
curl http://localhost:9000/siae-media/tenant-001/public/test.jpg -o downloaded.jpg

# 或在浏览器中直接访问
http://localhost:9000/siae-media/tenant-001/public/test.jpg
```

如果能成功下载文件，说明配置成功。

### 3. 测试私有路径

```bash
# 私有路径应该返回 403 Forbidden
curl http://localhost:9000/siae-media/tenant-001/private/test.jpg
# 预期输出: AccessDenied
```

## 路径规划

### 公开文件路径
```
{bucket}/{tenant-id}/public/{timestamp}/{filename}
```
示例：
```
siae-media/tenant-001/public/1699876543210/avatar.jpg
siae-media/tenant-001/public/1699876543210/article-image.png
```

### 私有文件路径
```
{bucket}/{tenant-id}/private/{timestamp}/{filename}
```
示例：
```
siae-media/tenant-001/private/1699876543210/draft-image.jpg
siae-media/tenant-001/private/1699876543210/confidential.pdf
```

## 安全注意事项

1. **只开放 public 路径**
   - 确保策略只匹配 `*/public/*` 路径
   - 私有文件路径 `*/private/*` 不应该被公开访问

2. **只读权限**
   - 公开访问只授予 `s3:GetObject` 权限（只读）
   - 不要授予 `s3:PutObject`、`s3:DeleteObject` 等写权限

3. **路径命名规范**
   - 公开文件必须上传到 `public` 路径
   - 私有文件必须上传到 `private` 路径
   - 应用层需要严格控制文件上传路径

4. **访问策略变更**
   - 如果文件从 PRIVATE 改为 PUBLIC，需要移动文件到 public 路径
   - 如果文件从 PUBLIC 改为 PRIVATE，需要移动文件到 private 路径
   - 或者在更新访问策略时清除 URL 缓存

## 常见问题

### Q1: 配置后仍然无法访问公开文件？

**A:** 检查以下几点：
1. 确认文件路径包含 `/public/`
2. 确认桶策略已正确应用：`mc anonymous get local/siae-media`
3. 确认 MinIO 服务正常运行
4. 检查防火墙是否阻止了 9000 端口

### Q2: 如何撤销公开访问？

**A:** 删除桶策略：
```bash
mc anonymous set none local/siae-media
```

### Q3: 可以为不同租户设置不同的策略吗？

**A:** 可以，修改策略的 Resource 部分：
```json
{
  "Resource": ["arn:aws:s3:::siae-media/tenant-001/public/*"]
}
```

### Q4: 生产环境建议？

**A:** 
1. 使用 CDN 加速公开文件访问
2. 配置 HTTPS 证书
3. 启用访问日志监控
4. 定期审计公开文件列表
5. 考虑使用对象存储的生命周期策略自动清理过期文件

## 相关文档

- [MinIO 官方文档 - Bucket Policy](https://min.io/docs/minio/linux/administration/identity-access-management/policy-based-access-control.html)
- [AWS S3 Policy 语法](https://docs.aws.amazon.com/AmazonS3/latest/userguide/access-policy-language-overview.html)
- [Media Service 重构方案](../REFACTORING_PLAN.md)
