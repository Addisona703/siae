# 内容服务 API 参数文档

> 本文档描述内容服务的发布和获取接口参数，支持笔记、文章、问题、视频、文件五种内容类型。

---

## 📤 发布内容接口

### 接口信息

| 项目 | 说明 |
|------|------|
| URL | `POST /api/v1/content` |
| Content-Type | `application/json` |
| 认证 | 需要 Bearer Token |

### 请求参数（ContentCreateDTO）

#### 通用字段

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| title | String | ✅ | 内容标题，最大200字符 | `"Spring Boot 3.0 新特性详解"` |
| type | String | ✅ | 内容类型：`article`/`note`/`question`/`video`/`file` | `"article"` |
| description | String | ✅ | 内容描述/摘要，最大500字符 | `"本文详细介绍了 Spring Boot 3.0 的主要新特性"` |
| coverFileId | String | ❌ | 封面文件ID（UUID） | `"550e8400-e29b-41d4-a716-446655440000"` |
| uploadedBy | Long | ✅ | 上传者用户ID | `10001` |
| categoryId | Long | ✅ | 分类ID | `1` |
| tagIds | List\<Long\> | ❌ | 标签ID列表 | `[1, 2, 3]` |
| status | String | ✅ | 状态：`DRAFT`-草稿，`PENDING`-待审核 | `"PENDING"` |
| detail | Object | ✅ | 内容详情，根据 type 不同结构不同 | 见下方 |

---

### 详情字段（detail）

#### 1. 文章详情（type = "article"）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| content | String | ✅ | 文章正文（支持 HTML/Markdown） | `"<p>Spring Boot 3.0 带来了许多新特性...</p>"` |

```json
{
  "title": "Spring Boot 3.0 新特性详解",
  "type": "article",
  "description": "本文详细介绍了 Spring Boot 3.0 的主要新特性",
  "coverFileId": "550e8400-e29b-41d4-a716-446655440000",
  "uploadedBy": 10001,
  "categoryId": 1,
  "tagIds": [1, 2, 3],
  "status": "PENDING",
  "detail": {
    "content": "<p>Spring Boot 3.0 带来了许多令人兴奋的新特性...</p>"
  }
}
```

---

#### 2. 笔记详情（type = "note"）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| content | String | ✅ | 笔记内容 | `"今天学习了 Spring Boot 的自动配置原理..."` |
| format | String | ❌ | 内容格式：`markdown`/`html`/`plain` | `"markdown"` |

```json
{
  "title": "Spring Boot 学习笔记",
  "type": "note",
  "description": "记录 Spring Boot 学习过程中的要点",
  "uploadedBy": 10001,
  "categoryId": 2,
  "tagIds": [1, 4],
  "status": "DRAFT",
  "detail": {
    "content": "# Spring Boot 自动配置\n\n## 核心原理\n...",
    "format": "markdown"
  }
}
```

---

#### 3. 问题详情（type = "question"）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| content | String | ✅ | 问题描述 | `"如何在 Spring Boot 中配置多数据源？"` |
| status | String | ❌ | 问题状态：`OPEN`-未解决，`SOLVED`-已解决 | `"OPEN"` |

```json
{
  "title": "Spring Boot 多数据源配置问题",
  "type": "question",
  "description": "在项目中需要连接多个数据库，求解决方案",
  "uploadedBy": 10001,
  "categoryId": 3,
  "tagIds": [1, 5],
  "status": "PENDING",
  "detail": {
    "content": "我在项目中需要同时连接 MySQL 和 PostgreSQL，请问如何配置？",
    "status": "OPEN"
  }
}
```

---

#### 4. 视频详情（type = "video"）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| videoFileId | String | ✅ | 视频文件ID（UUID），关联 Media 服务 | `"550e8400-e29b-41d4-a716-446655440000"` |

```json
{
  "title": "Spring Boot 入门教程",
  "type": "video",
  "description": "从零开始学习 Spring Boot 框架",
  "coverFileId": "cover-file-uuid",
  "uploadedBy": 10001,
  "categoryId": 4,
  "tagIds": [1, 6],
  "status": "PENDING",
  "detail": {
    "videoFileId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

#### 5. 文件详情（type = "file"）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| fileId | String | ✅ | 文件ID（UUID），关联 Media 服务 | `"550e8400-e29b-41d4-a716-446655440000"` |

```json
{
  "title": "Spring Boot 项目文档",
  "type": "file",
  "description": "完整的项目开发文档 PDF",
  "coverFileId": "cover-file-uuid",
  "uploadedBy": 10001,
  "categoryId": 5,
  "tagIds": [1, 7],
  "status": "PENDING",
  "detail": {
    "fileId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

### 响应示例

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1001,
    "title": "Spring Boot 3.0 新特性详解",
    "type": "ARTICLE",
    "status": "PENDING",
    "createTime": "2025-12-08T10:30:00"
  }
}
```

---

## 📥 获取内容接口

### 1. 分页查询内容列表

#### 接口信息

| 项目 | 说明 |
|------|------|
| URL | `GET /api/v1/content` |
| Content-Type | `application/json` |
| 认证 | 可选（影响权限过滤） |

#### 请求参数（ContentQueryDTO）

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| categoryId | Long | ❌ | 分类ID | `1` |
| tagIds | List\<Long\> | ❌ | 标签ID列表 | `[1, 2]` |
| type | String | ❌ | 内容类型：`ARTICLE`/`NOTE`/`QUESTION`/`VIDEO`/`FILE` | `"ARTICLE"` |
| status | String | ❌ | 内容状态：`DRAFT`/`PENDING`/`PUBLISHED`/`REJECTED` | `"PUBLISHED"` |
| keyword | String | ❌ | 搜索关键词 | `"Spring Boot"` |
| page | Integer | ❌ | 页码，默认 1 | `1` |
| size | Integer | ❌ | 每页数量，默认 10 | `10` |

#### 请求示例

```
GET /api/v1/content?type=ARTICLE&status=PUBLISHED&keyword=Spring&page=1&size=10
```

#### 响应示例（列表）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pages": 10,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1001,
        "title": "Spring Boot 3.0 新特性详解",
        "type": "ARTICLE",
        "description": "本文详细介绍了 Spring Boot 3.0 的主要新特性...",
        "coverUrl": "https://cdn.example.com/covers/spring-boot.jpg",
        "uploadedBy": 10001,
        "authorNickname": "技术达人",
        "authorAvatarUrl": "https://cdn.example.com/avatars/user.jpg",
        "categoryName": "技术文章",
        "status": "PUBLISHED",
        "tagNames": ["Java", "Spring", "后端"],
        "statistics": {
          "viewCount": 1580,
          "likeCount": 256,
          "commentCount": 32,
          "favoriteCount": 128
        },
        "createTime": "2025-12-08T10:30:00",
        "updateTime": "2025-12-08T10:30:00"
      }
    ]
  }
}
```

---

### 2. 获取内容详情

#### 接口信息

| 项目 | 说明 |
|------|------|
| URL | `GET /api/v1/content/{id}` |
| 认证 | 可选（影响权限过滤） |

#### 路径参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 内容ID |

#### 响应示例（文章详情）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "title": "Spring Boot 3.0 新特性详解",
    "type": "ARTICLE",
    "description": "本文详细介绍了 Spring Boot 3.0 的主要新特性",
    "coverFileId": "550e8400-e29b-41d4-a716-446655440000",
    "coverUrl": "https://cdn.example.com/covers/spring-boot.jpg",
    "uploadedBy": 10001,
    "authorNickname": "技术达人",
    "authorAvatarUrl": "https://cdn.example.com/avatars/user.jpg",
    "categoryName": "技术文章",
    "status": "PUBLISHED",
    "tagNames": ["Java", "Spring", "后端"],
    "statistics": {
      "viewCount": 1580,
      "likeCount": 256,
      "commentCount": 32,
      "favoriteCount": 128
    },
    "detail": {
      "id": 1,
      "contentId": 1001,
      "content": "<p>Spring Boot 3.0 带来了许多令人兴奋的新特性...</p>",
      "createTime": "2025-12-08T10:30:00",
      "updateTime": "2025-12-08T10:30:00"
    },
    "createTime": "2025-12-08T10:30:00",
    "updateTime": "2025-12-08T10:30:00"
  }
}
```

---

### 3. 各类型详情响应结构

#### 文章详情（ArticleVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 文章详情ID |
| contentId | Long | 关联内容ID |
| content | String | 文章正文内容 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

#### 笔记详情（NoteVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 笔记详情ID |
| contentId | Long | 关联内容ID |
| content | String | 笔记内容 |
| format | String | 内容格式：markdown/html/plain |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

#### 问题详情（QuestionVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 问题详情ID |
| contentId | Long | 关联内容ID |
| content | String | 问题内容 |
| answerCount | Integer | 回答数量 |
| solved | String | 问题状态：OPEN/SOLVED |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

#### 视频详情（VideoVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 视频详情ID |
| contentId | Long | 关联内容ID |
| videoFileId | String | 视频文件ID |
| playCount | Integer | 播放次数 |
| duration | Integer | 视频时长（秒），从 Media 服务获取 |
| resolution | String | 视频分辨率，从 Media 服务获取 |
| filename | String | 文件名，从 Media 服务获取 |
| size | Long | 文件大小（字节），从 Media 服务获取 |
| mime | String | MIME 类型，从 Media 服务获取 |
| url | String | 视频访问 URL |
| available | Boolean | Media 服务是否可用 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

#### 文件详情（FileVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 文件详情ID |
| contentId | Long | 关联内容ID |
| fileId | String | 文件ID |
| downloadCount | Integer | 下载次数 |
| fileName | String | 文件名，从 Media 服务获取 |
| fileSize | Long | 文件大小（字节），从 Media 服务获取 |
| fileType | String | 文件 MIME 类型，从 Media 服务获取 |
| url | String | 文件访问 URL |
| available | Boolean | Media 服务是否可用 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

## 📊 统计信息（StatisticsVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| viewCount | Integer | 浏览次数 |
| likeCount | Integer | 点赞数 |
| commentCount | Integer | 评论数 |
| favoriteCount | Integer | 收藏数 |

---

## 🔐 状态枚举

### 内容状态（ContentStatusEnum）

| 值 | 说明 |
|------|------|
| DRAFT | 草稿 |
| PENDING | 待审核 |
| PUBLISHED | 已发布 |
| REJECTED | 已拒绝 |
| DELETED | 已删除 |

### 内容类型（ContentTypeEnum）

| 值 | 说明 |
|------|------|
| ARTICLE | 文章 |
| NOTE | 笔记 |
| QUESTION | 问题 |
| VIDEO | 视频 |
| FILE | 文件 |

### 问题状态（QuestionStatusEnum）

| 值 | 说明 |
|------|------|
| OPEN | 未解决 |
| SOLVED | 已解决 |

---

## ⚠️ 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限访问 |
| 404 | 内容不存在 |
| 500 | 服务器内部错误 |

---

**维护团队**: SIAE Team  
**最后更新**: 2025-12-08
