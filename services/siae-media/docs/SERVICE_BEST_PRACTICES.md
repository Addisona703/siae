# Service 层最佳实践

## 📋 目录
- [数据转换](#数据转换)
- [复杂查询](#复杂查询)
- [事务管理](#事务管理)
- [异常处理](#异常处理)

---

## 数据转换

### ✅ 推荐：使用 BeanConvertUtil

**位置**：`com.hngy.siae.core.utils.BeanConvertUtil`

#### 1️⃣ 单个对象转换

```java
// Entity → DTO/VO
FileInfoResponse response = BeanConvertUtil.to(fileEntity, FileInfoResponse.class);

// 如果字段名不一致，手动设置
response.setFileId(fileEntity.getId());
```

#### 2️⃣ 批量转换

```java
// List<Entity> → List<DTO>
List<FileInfoResponse> responses = BeanConvertUtil.toList(entities, FileInfoResponse.class);

// 如果需要额外处理
for (int i = 0; i < responses.size(); i++) {
    responses.get(i).setFileId(entities.get(i).getId());
}
```

#### 3️⃣ 部分字段复制

```java
// 复制属性，排除某些字段
BeanConvertUtil.to(source, target, "password", "salt");
```

### ❌ 不推荐：手动逐字段赋值

```java
// ❌ 不推荐 - 代码冗长，容易遗漏字段
FileInfoResponse response = new FileInfoResponse();
response.setFileId(entity.getId());
response.setTenantId(entity.getTenantId());
response.setOwnerId(entity.getOwnerId());
response.setBucket(entity.getBucket());
// ... 20+ 行代码
```

---

## 复杂查询

### ✅ 推荐：使用 @Select 注解 + XML SQL

对于复杂的查询（多表关联、子查询、聚合等），推荐使用 MyBatis 的 XML 或注解方式。

#### 方式一：XML Mapper（推荐）

**Mapper 接口：**
```java
@Mapper
public interface FileRepository extends BaseMapper<FileEntity> {
    
    /**
     * 复杂查询：根据多条件查询文件，包含标签匹配
     */
    List<FileEntity> selectFilesByComplexConditions(@Param("query") FileQueryRequest query);
}
```

**XML 文件：** `FileRepository.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.hngy.siae.media.repository.FileRepository">
    
    <select id="selectFilesByComplexConditions" resultType="com.hngy.siae.media.domain.entity.FileEntity">
        SELECT 
            f.*,
            COUNT(d.id) as derivative_count
        FROM media_file f
        LEFT JOIN media_file_derivative d ON f.id = d.file_id
        WHERE f.deleted_at IS NULL
        <if test="query.tenantId != null">
            AND f.tenant_id = #{query.tenantId}
        </if>
        <if test="query.ownerId != null">
            AND f.owner_id = #{query.ownerId}
        </if>
        <if test="query.status != null">
            AND f.status = #{query.status}
        </if>
        <if test="query.bizTags != null and query.bizTags.size() > 0">
            <foreach collection="query.bizTags" item="tag" separator=" AND ">
                AND JSON_CONTAINS(f.biz_tags, JSON_QUOTE(#{tag}))
            </foreach>
        </if>
        <if test="query.createdFrom != null">
            AND f.created_at >= #{query.createdFrom}
        </if>
        <if test="query.createdTo != null">
            AND f.created_at &lt;= #{query.createdTo}
        </if>
        GROUP BY f.id
        <choose>
            <when test="query.orderBy == 'size'">
                ORDER BY f.size ${query.order}
            </when>
            <otherwise>
                ORDER BY f.created_at DESC
            </otherwise>
        </choose>
    </select>
    
</mapper>
```

#### 方式二：@Select 注解（简单查询）

```java
@Mapper
public interface FileRepository extends BaseMapper<FileEntity> {
    
    /**
     * 查询租户的文件统计
     */
    @Select("""
        SELECT 
            tenant_id,
            COUNT(*) as file_count,
            SUM(size) as total_size
        FROM media_file
        WHERE tenant_id = #{tenantId}
          AND deleted_at IS NULL
        GROUP BY tenant_id
        """)
    FileStatistics selectFileStatistics(@Param("tenantId") String tenantId);
}
```

### ⚠️ 谨慎使用：LambdaQueryWrapper

**适用场景**：简单的单表查询

```java
// ✅ 简单查询 - 可以使用 Wrapper
LambdaQueryWrapper<FileEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(FileEntity::getTenantId, tenantId)
       .eq(FileEntity::getStatus, FileStatus.COMPLETED)
       .isNull(FileEntity::getDeletedAt)
       .orderByDesc(FileEntity::getCreatedAt);
```

**不适用场景**：复杂查询

```java
// ❌ 不推荐 - 复杂的 JSON 查询、多表关联
wrapper.apply("JSON_CONTAINS(biz_tags, JSON_QUOTE({0}))", tag)  // 难以维护
       .apply("EXISTS (SELECT 1 FROM ...)")  // 可读性差
       .last("LIMIT 100");  // SQL 注入风险
```

### 📝 复杂查询示例

#### 示例 1：多表关联 + 聚合

```xml
<!-- 查询文件及其衍生文件数量 -->
<select id="selectFilesWithDerivativeCount" resultType="FileWithDerivativeDTO">
    SELECT 
        f.id,
        f.tenant_id,
        f.storage_key,
        f.size,
        f.mime,
        COUNT(DISTINCT d.id) as derivative_count,
        COUNT(DISTINCT a.id) as audit_log_count
    FROM media_file f
    LEFT JOIN media_file_derivative d ON f.id = d.file_id
    LEFT JOIN media_audit_log a ON f.id = a.file_id
    WHERE f.tenant_id = #{tenantId}
      AND f.deleted_at IS NULL
    GROUP BY f.id
    HAVING derivative_count > 0
    ORDER BY f.created_at DESC
    LIMIT #{limit}
</select>
```

#### 示例 2：子查询

```xml
<!-- 查询超过配额的租户 -->
<select id="selectTenantsExceedingQuota" resultType="TenantQuotaDTO">
    SELECT 
        t.tenant_id,
        t.bytes_used,
        t.objects_count,
        (SELECT JSON_EXTRACT(limits, '$.max_bytes') FROM media_quota WHERE tenant_id = t.tenant_id) as max_bytes
    FROM (
        SELECT 
            tenant_id,
            SUM(size) as bytes_used,
            COUNT(*) as objects_count
        FROM media_file
        WHERE deleted_at IS NULL
        GROUP BY tenant_id
    ) t
    WHERE t.bytes_used > (
        SELECT JSON_EXTRACT(limits, '$.max_bytes')
        FROM media_quota
        WHERE tenant_id = t.tenant_id
    )
</select>
```

#### 示例 3：窗口函数

```xml
<!-- 查询每个租户最新的 10 个文件 -->
<select id="selectLatestFilesByTenant" resultType="FileEntity">
    SELECT * FROM (
        SELECT 
            *,
            ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY created_at DESC) as rn
        FROM media_file
        WHERE deleted_at IS NULL
    ) ranked
    WHERE rn &lt;= 10
</select>
```

---

## 事务管理

### ✅ 推荐做法

```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    /**
     * 使用 @Transactional 注解
     * - rollbackFor: 指定回滚的异常类型
     * - 默认只回滚 RuntimeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(String fileId) {
        // 1. 更新文件状态
        fileRepository.updateById(file);
        
        // 2. 更新配额
        quotaService.decreaseUsage(tenantId, size, 1);
        
        // 3. 记录审计日志
        auditService.logFileDelete(fileId, tenantId, userId, metadata);
        
        // 如果任何一步失败，整个事务回滚
    }
}
```

### ❌ 不推荐

```java
// ❌ 不推荐 - 没有事务管理
public void deleteFile(String fileId) {
    fileRepository.updateById(file);  // 成功
    quotaService.decreaseUsage(...);  // 失败 - 但文件已更新！
    // 数据不一致
}
```

---

## 异常处理

### ✅ 推荐：使用业务异常

```java
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.asserts.AssertUtils;

@Service
public class FileService {
    
    public FileInfoResponse getFileById(String fileId) {
        FileEntity file = fileRepository.selectById(fileId);
        
        // 使用 AssertUtils 抛出业务异常
        AssertUtils.notNull(file, "文件不存在");
        AssertUtils.isNull(file.getDeletedAt(), "文件已删除");
        
        return BeanConvertUtil.to(file, FileInfoResponse.class);
    }
}
```

### ❌ 不推荐

```java
// ❌ 不推荐 - 使用通用异常
throw new RuntimeException("文件不存在");  // 不够明确

// ❌ 不推荐 - 返回 null
if (file == null) {
    return null;  // 调用方需要判空，容易出错
}
```

---

## 总结

### 数据转换
- ✅ 使用 `BeanConvertUtil.to()` 和 `BeanConvertUtil.toList()`
- ❌ 避免手动逐字段赋值

### 复杂查询
- ✅ 使用 XML Mapper 或 `@Select` 注解
- ⚠️ 简单查询可以用 `LambdaQueryWrapper`
- ❌ 避免在 Wrapper 中写复杂的 SQL 片段

### 事务管理
- ✅ 使用 `@Transactional(rollbackFor = Exception.class)`
- ❌ 避免遗漏事务注解

### 异常处理
- ✅ 使用 `AssertUtils` 抛出业务异常
- ❌ 避免使用通用异常或返回 null
