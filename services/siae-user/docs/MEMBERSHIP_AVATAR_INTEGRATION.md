# 成员头像URL集成说明

## 概述

成员服务已集成Media服务，在获取成员信息时自动返回大头照（headshot）的访问URL。

## 功能特性

### 1. 自动填充大头照URL

所有成员查询接口都会自动填充 `headshotUrl` 字段：

- ✅ 单个成员查询：`getMembershipById()`
- ✅ 按用户ID查询：`getMembershipByUserId()`
- ✅ 成员列表查询：`pageMemberships()`
- ✅ 成员详情查询：`getMembershipDetailById()`（包含头像和大头照）

### 2. 字段说明

**MembershipVO**:
- `headshotFileId` - 大头照文件ID（保留）
- `headshotUrl` - 大头照访问URL（新增）

**MembershipDetailVO**:
- `avatarFileId` - 用户头像文件ID（保留）
- `avatarUrl` - 用户头像访问URL（新增）
- `headshotFileId` - 成员大头照文件ID（保留）
- `headshotUrl` - 成员大头照访问URL（新增）

### 3. 批量优化

成员列表查询使用批量接口，一次性获取所有大头照URL：

```
100个成员列表：
- 传统方式：100次Media服务调用 ≈ 2000ms
- 批量方式：1次Media服务调用 ≈ 50ms（含缓存）
```

## 接口响应示例

### 单个成员查询

**请求**: `GET /api/v1/user/memberships/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 100,
    "headshotFileId": "headshot123",
    "headshotUrl": "https://minio.example.com/bucket/headshot.jpg?signature=...",
    "lifecycleStatus": "OFFICIAL",
    "joinDate": "2024-01-01",
    "realName": "张三",
    "departmentName": "技术部",
    "positionName": "部长"
  }
}
```

### 成员列表查询

**请求**: `POST /api/v1/user/memberships/page`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 100,
        "headshotFileId": "headshot123",
        "headshotUrl": "https://minio.example.com/bucket/headshot1.jpg?signature=...",
        "realName": "张三"
      },
      {
        "id": 2,
        "userId": 101,
        "headshotFileId": "headshot456",
        "headshotUrl": "https://minio.example.com/bucket/headshot2.jpg?signature=...",
        "realName": "李四"
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 成员详情查询

**请求**: `GET /api/v1/user/memberships/{id}/detail`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 100,
    "username": "zhangsan",
    "avatarFileId": "avatar123",
    "avatarUrl": "https://minio.example.com/bucket/avatar.jpg?signature=...",
    "headshotFileId": "headshot123",
    "headshotUrl": "https://minio.example.com/bucket/headshot.jpg?signature=...",
    "realName": "张三",
    "email": "zhangsan@example.com",
    "lifecycleStatus": 1,
    "lifecycleStatusName": "正式成员",
    "joinDate": "2024-01-01",
    "departments": [
      {
        "departmentId": 1,
        "departmentName": "技术部"
      }
    ],
    "positions": [
      {
        "positionId": 1,
        "positionName": "部长"
      }
    ],
    "awards": ["优秀成员", "技术之星"]
  }
}
```

## 技术实现

### 服务层集成

```java
// 单个成员
private void enrichMembershipWithHeadshotUrl(MembershipVO membershipVO) {
    if (StrUtil.isBlank(membershipVO.getHeadshotFileId())) {
        return;
    }
    Result<String> result = mediaFeignClient.getFileUrl(
        membershipVO.getHeadshotFileId(), 86400
    );
    membershipVO.setHeadshotUrl(result.getData());
}

// 成员列表（批量）
private void enrichMembershipsWithHeadshotUrls(List<MembershipVO> memberships) {
    List<String> headshotIds = memberships.stream()
        .map(MembershipVO::getHeadshotFileId)
        .filter(StrUtil::isNotBlank)
        .distinct()
        .collect(Collectors.toList());
    
    Map<String, String> urls = batchGetMediaUrls(headshotIds);
    
    memberships.forEach(membership -> {
        if (StrUtil.isNotBlank(membership.getHeadshotFileId())) {
            membership.setHeadshotUrl(urls.get(membership.getHeadshotFileId()));
        }
    });
}

// 成员详情（头像+大头照）
private void enrichMembershipDetailWithMediaUrls(MembershipDetailVO detail) {
    List<String> fileIds = new ArrayList<>();
    
    if (StrUtil.isNotBlank(detail.getAvatarFileId())) {
        fileIds.add(detail.getAvatarFileId());
    }
    if (StrUtil.isNotBlank(detail.getHeadshotFileId())) {
        fileIds.add(detail.getHeadshotFileId());
    }
    
    Map<String, String> urls = batchGetMediaUrls(fileIds);
    detail.setAvatarUrl(urls.get(detail.getAvatarFileId()));
    detail.setHeadshotUrl(urls.get(detail.getHeadshotFileId()));
}
```

## 容错设计

### 1. Media服务故障

```java
try {
    Map<String, String> urls = batchGetMediaUrls(headshotIds);
    membership.setHeadshotUrl(urls.get(membership.getHeadshotFileId()));
} catch (Exception e) {
    log.warn("Failed to get headshot URL", e);
    // headshotUrl为null，不影响成员信息返回
}
```

### 2. 部分文件不存在

Media服务只返回存在的文件URL，不存在的文件不会出现在返回结果中。

## 使用场景

### 1. 成员展示墙

```javascript
// 获取所有正式成员
const members = await api.getMemberList({ 
  lifecycleStatus: 1,  // 正式成员
  pageSize: 100 
});

// 所有成员的headshotUrl都已填充，可以直接显示
members.records.forEach(member => {
  displayMemberCard(member.realName, member.headshotUrl);
});
```

### 2. 成员详情页

```javascript
// 获取成员详情
const detail = await api.getMemberDetail(memberId);

// 显示用户头像和成员大头照
displayAvatar(detail.avatarUrl);        // 用户头像
displayHeadshot(detail.headshotUrl);    // 成员大头照
```

### 3. 候选成员转正

```javascript
// 候选成员转正时，可以上传新的大头照
await api.promoteToOfficial({
  id: memberId,
  joinDate: '2024-01-01',
  headshotFileId: 'new-headshot-id'  // 新的大头照
});

// 转正后查询，会自动返回新的headshotUrl
const member = await api.getMemberById(memberId);
```

## 注意事项

1. **向后兼容**: `headshotFileId` 字段保留，前端可以选择使用ID或URL
2. **容错设计**: Media服务故障不影响成员信息查询
3. **性能优先**: 列表查询使用批量接口，避免N+1问题
4. **URL有效期**: 24小时，与用户头像URL策略一致

## 相关文档

- [用户头像URL集成说明](./AVATAR_URL_INTEGRATION.md)
- [Media服务批量URL API文档](../../siae-media/docs/BATCH_URL_API.md)
