# API-Media 使用示例

## 场景1：内容服务调用媒体服务获取文件信息

### 1. 添加依赖

在 `siae-content` 的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.hngy</groupId>
    <artifactId>api-media</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用 Feign 客户端

```java
@EnableFeignClients(basePackages = {
    "com.hngy.siae.api.media.client",
    "com.hngy.siae.api.user.client"
})
@SpringBootApplication
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }
}
```

### 3. 在 Service 中使用

```java
package com.hngy.siae.content.service.impl;

import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.response.FileInfoVO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    
    private final MediaFeignClient mediaFeignClient;
    
    @Override
    public ContentDetailVO getContentDetail(Long contentId) {
        // 1. 查询内容基本信息
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new BusinessException("内容不存在"));
        
        // 2. 如果内容有关联的媒体文件，获取文件信息
        if (content.getCoverImageId() != null) {
            try {
                // 获取封面图片URL（1小时有效期）
                String coverUrl = mediaFeignClient.getFileUrl(
                    content.getCoverImageId(), 
                    3600
                );
                content.setCoverUrl(coverUrl);
            } catch (ServiceException e) {
                log.error("获取封面图片失败: {}", e.getMessage());
                // 降级处理：使用默认图片
                content.setCoverUrl("/default-cover.jpg");
            }
        }
        
        // 3. 如果有附件列表，批量获取URL
        if (!content.getAttachmentIds().isEmpty()) {
            try {
                BatchUrlDTO request = new BatchUrlDTO();
                request.setFileIds(content.getAttachmentIds());
                request.setExpirySeconds(7200); // 2小时有效期
                
                BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
                content.setAttachmentUrls(result.getUrls());
            } catch (ServiceException e) {
                log.error("批量获取附件URL失败: {}", e.getMessage());
            }
        }
        
        return BeanUtil.toBean(content, ContentDetailVO.class);
    }
}
```

## 场景2：用户服务获取用户头像URL

```java
package com.hngy.siae.user.service.impl;

import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private final MediaFeignClient mediaFeignClient;
    
    /**
     * 批量获取用户信息（包含头像URL）
     */
    public List<UserProfileVO> batchGetUserProfiles(List<Long> userIds) {
        // 1. 查询用户基本信息
        List<User> users = userRepository.findAllById(userIds);
        
        // 2. 提取所有头像文件ID
        List<String> avatarFileIds = users.stream()
            .map(User::getAvatarFileId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        // 3. 批量获取头像URL
        Map<String, String> avatarUrls = Map.of();
        if (!avatarFileIds.isEmpty()) {
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(avatarFileIds);
            request.setExpirySeconds(86400); // 24小时有效期
            
            BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
            avatarUrls = result.getUrls();
        }
        
        // 4. 组装返回数据
        Map<String, String> finalAvatarUrls = avatarUrls;
        return users.stream()
            .map(user -> {
                UserProfileVO vo = new UserProfileVO();
                vo.setUserId(user.getId());
                vo.setUsername(user.getUsername());
                vo.setAvatarUrl(finalAvatarUrls.getOrDefault(
                    user.getAvatarFileId(), 
                    "/default-avatar.png"
                ));
                return vo;
            })
            .collect(Collectors.toList());
    }
}
```

## 场景3：异常处理和降级策略

```java
package com.hngy.siae.content.service.impl;

import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaIntegrationService {
    
    private final MediaFeignClient mediaFeignClient;
    
    /**
     * 获取文件URL（带重试和降级）
     */
    public String getFileUrlWithFallback(String fileId) {
        try {
            return mediaFeignClient.getFileUrl(fileId, 3600);
        } catch (ServiceException e) {
            if (e.getCode() == 503) {
                log.warn("媒体服务不可用，使用降级策略");
                // 降级策略1：返回占位符URL
                return "/placeholder.jpg";
            } else if (e.getCode() == 404) {
                log.warn("文件不存在: {}", fileId);
                // 降级策略2：返回默认图片
                return "/default.jpg";
            }
            throw e;
        }
    }
    
    /**
     * 批量获取文件URL（部分失败容错）
     */
    public Map<String, String> batchGetFileUrlsWithFallback(List<String> fileIds) {
        try {
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(fileIds);
            request.setExpirySeconds(7200);
            
            BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
            
            // 记录失败数量
            if (result.getFailedCount() > 0) {
                log.warn("批量获取URL部分失败，成功: {}, 失败: {}", 
                    result.getSuccessCount(), result.getFailedCount());
            }
            
            return result.getUrls();
        } catch (ServiceException e) {
            log.error("批量获取URL完全失败: {}", e.getMessage());
            // 返回空Map，调用方需要处理
            return Map.of();
        }
    }
}
```

## 场景4：缓存优化

```java
package com.hngy.siae.content.service.impl;

import com.hngy.siae.api.media.client.MediaFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaCacheService {
    
    private final MediaFeignClient mediaFeignClient;
    
    /**
     * 获取文件URL（带缓存）
     * 缓存时间应小于URL有效期
     */
    @Cacheable(value = "fileUrls", key = "#fileId", unless = "#result == null")
    public String getCachedFileUrl(String fileId) {
        // URL有效期24小时，缓存23小时
        return mediaFeignClient.getFileUrl(fileId, 86400);
    }
    
    /**
     * 获取文件信息（带缓存）
     */
    @Cacheable(value = "fileInfo", key = "#fileId", unless = "#result == null")
    public FileInfoVO getCachedFileInfo(String fileId) {
        return mediaFeignClient.getFileById(fileId);
    }
}
```

## 配置说明

### application.yml

```yaml
# Feign 配置
feign:
  client:
    config:
      siae-media:  # 针对 media 服务的配置
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: BASIC
  
  # 启用断路器
  circuitbreaker:
    enabled: true

# 断路器配置
resilience4j:
  circuitbreaker:
    instances:
      siae-media:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3

# 缓存配置
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=23h
```

## 最佳实践

1. **合理设置URL有效期**
   - 公开内容：24小时或更长
   - 私密内容：1-2小时
   - 临时预览：15-30分钟

2. **使用批量接口**
   - 需要多个文件URL时，优先使用 `batchGetFileUrls`
   - 减少网络往返次数，提升性能

3. **实现降级策略**
   - 捕获 `ServiceException` 并提供默认值
   - 记录错误日志便于排查问题

4. **添加缓存**
   - 对频繁访问的文件URL进行缓存
   - 缓存时间应小于URL有效期

5. **异常处理**
   - 区分不同的错误码（503服务不可用、404文件不存在等）
   - 根据错误类型采取不同的处理策略

---

**更新时间**: 2024-11-26
