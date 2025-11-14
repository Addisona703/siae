# Content服务

## 服务概述

Content服务负责管理内容发布、分类、标签、评论、审核以及用户交互等功能。服务基础路径：`/api/v1/content`

---

## 数据库设计

### 1. 核心业务表（7张）
1. **content** - 内容主表
2. **article** - 文章详情表
3. **question** - 问题详情表
4. **note** - 笔记详情表
5. **file** - 文件详情表
6. **video** - 视频详情表
7. **comment** - 评论表

### 2. 字典表部分（2张）
1. **category** - 分类表
2. **tag** - 标签表

### 3. 关联表部分（1张）
1. **tag_relation** - 内容标签关联表

### 4. 统计与行为表（3张）
1. **statistics** - 内容统计表
2. **user_action** - 用户行为记录表
3. **audit** - 审核记录表

### 设计说明
- **内容主表设计：** 采用主表+详情表的设计模式，content表存储所有类型内容的公共信息
  - 优势：统一管理不同类型内容，便于统计和查询
  - 内容类型：0文章、1笔记、2提问、3文件、4视频
- **详情表设计：** 根据内容类型关联不同的详情表（article、note、question、file、video）
  - 优势：不同类型内容的特有字段分离存储，避免字段冗余
- **分类表设计：** 支持树形结构，通过parent_id实现父子分类关系
- **标签关联：** 通过tag_relation表实现内容与标签的多对多关系
- **统计表设计：** 独立的statistics表记录浏览、点赞、收藏、评论等统计数据
- **用户行为：** user_action表记录用户的所有交互行为（浏览、点赞、收藏、举报等）

![image-20251113162603057](C:\Users\31833\Desktop\Siae Studio\siae\services\siae-content\docs\images\content-service-view.png)

---

## 接口设计

### 核心业务控制器

#### 一、内容控制器 (ContentController)

**基础路径：** `/api/v1/content`

##### 1. 发布内容
- **接口：** `POST /api/v1/content/`
- **描述：** 创建并发布新的内容，支持文章、笔记、提问、文件、视频等多种类型
- **权限：** `CONTENT_PUBLISH`
- **请求体：**
```json
{
  "title": "Spring Boot 教程",
  "type": 0,
  "description": "全面介绍Spring Boot的使用方法",
  "categoryId": 3,
  "uploadedBy": 1,
  "status": 2,
  "tagIds": [1, 2],
  "detail": {
    "content": "Spring Boot 是一个快速开发框架...",
    "coverUrl": "https://example.com/cover.png"
  }
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Spring Boot 教程",
    "type": 0,
    "description": "全面介绍Spring Boot的使用方法",
    "categoryId": 3,
    "uploadedBy": 1,
    "status": 2,
    "createTime": "2024-05-18T10:00:00",
    "updateTime": "2024-05-18T10:00:00",
    "detail": {
      "content": "Spring Boot 是一个快速开发框架...",
      "coverUrl": "https://example.com/cover.png"
    }
  }
}
```

##### 2. 编辑内容
- **接口：** `PUT /api/v1/content/`
- **描述：** 修改已存在的内容信息，包括标题、描述、分类等
- **权限：** `CONTENT_EDIT`
- **请求体：** （所有字段均为可选，只更新提供的字段）
```json
{
  "id": 1,
  "title": "Spring Boot 进阶教程",
  "description": "深入讲解Spring Boot高级特性",
  "categoryId": 3,
  "tagIds": [1, 2, 3],
  "detail": {
    "content": "更新后的内容...",
    "coverUrl": "https://example.com/new-cover.png"
  }
}
```
- **响应：** 同发布内容响应

##### 3. 删除内容
- **接口：** `DELETE /api/v1/content/`
- **描述：** 删除指定的内容，可选择永久删除或移至垃圾箱
- **权限：** `CONTENT_DELETE`
- **查询参数：**
  - `id` - 内容ID（必填）
  - `isTrash` - 是否移至垃圾箱（0-永久删除，1-移至垃圾箱）
- **示例：** `DELETE /api/v1/content/?id=1&isTrash=1`
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

##### 4. 查询内容详情
- **接口：** `GET /api/v1/content/query/{contentId}`
- **描述：** 根据内容ID获取内容的详细信息，包括基本信息和具体内容详情
- **权限：** `CONTENT_QUERY`
- **路径参数：** `contentId` - 内容ID
- **响应：** 同发布内容响应

##### 5. 分页查询内容列表
- **接口：** `POST /api/v1/content/page`
- **描述：** 分页查询内容列表，支持按分类、标签、状态等条件筛选
- **权限：** `CONTENT_LIST_VIEW`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "type": 0,
    "categoryId": 3,
    "status": 2,
    "uploadedBy": 1
  },
  "keyword": "Spring"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 6. 查询热门内容
- **接口：** `GET /api/v1/content/hot`
- **描述：** 查询热门内容列表（根据浏览量、点赞数等排序）
- **权限：** 公开接口
- **查询参数：**
  - `pageNum` - 页码（默认1）
  - `pageSize` - 每页大小（默认10）
  - `type` - 内容类型（可选）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

#### 二、评论控制器 (CommentsController)

**基础路径：** `/api/v1/content/comments`

##### 1. 创建评论
- **接口：** `POST /api/v1/content/comments/{contentId}`
- **描述：** 为指定内容创建评论
- **权限：** 已认证用户
- **路径参数：** `contentId` - 内容ID
- **请求体：**
```json
{
  "userId": 2,
  "parentId": null,
  "content": "写得很好，受益匪浅！"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "contentId": 1,
    "userId": 2,
    "parentId": null,
    "content": "写得很好，受益匪浅！",
    "status": 0,
    "createTime": "2024-05-21T10:00:00"
  }
}
```

##### 2. 更新评论
- **接口：** `PUT /api/v1/content/comments/{commentId}`
- **描述：** 更新指定评论的内容
- **权限：** 评论作者或管理员
- **路径参数：** `commentId` - 评论ID
- **请求体：**
```json
{
  "content": "更新后的评论内容"
}
```
- **响应：** 同创建评论响应

##### 3. 删除评论
- **接口：** `DELETE /api/v1/content/comments/{id}`
- **描述：** 删除指定的评论
- **权限：** 评论作者或管理员
- **路径参数：** `id` - 评论ID
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

##### 4. 查询评论列表（旧版）
- **接口：** `GET /api/v1/content/comments/{contentId}`
- **描述：** 根据内容ID分页查询评论列表（保持向后兼容）
- **权限：** 公开接口
- **路径参数：** `contentId` - 内容ID
- **查询参数：**
  - `page` - 页码（默认1）
  - `size` - 每页大小（默认10）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 20,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 5. 分页查询评论（标准化）
- **接口：** `POST /api/v1/content/comments/page`
- **描述：** 使用标准化分页参数查询评论列表
- **权限：** 公开接口
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "contentId": 1,
    "userId": 2,
    "status": 1
  }
}
```
- **响应：** 同查询评论列表响应

---

#### 三、用户交互控制器 (InteractionsController)

**基础路径：** `/api/v1/content/interactions`

##### 1. 记录用户行为
- **接口：** `POST /api/v1/content/interactions/action`
- **描述：** 记录用户对内容的交互行为，如点赞、收藏、浏览等
- **权限：** `CONTENT_INTERACTION_RECORD`
- **请求体：**
```json
{
  "userId": 2,
  "targetId": 1,
  "targetType": 0,
  "actionType": 1
}
```
- **说明：**
  - `targetType`: 0-content, 1-comment, 2-user
  - `actionType`: 0-view, 1-like, 2-favorite, 3-report
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

##### 2. 取消用户行为
- **接口：** `DELETE /api/v1/content/interactions/action`
- **描述：** 取消用户对内容的交互行为，如取消点赞、取消收藏等
- **权限：** `CONTENT_INTERACTION_CANCEL`
- **请求体：** 同记录用户行为
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

#### 四、审核控制器 (AuditsController)

**基础路径：** `/api/v1/content/audits`

##### 1. 处理审核
- **接口：** `PUT /api/v1/content/audits/{id}`
- **描述：** 处理内容审核，包括审核通过、拒绝等操作
- **权限：** `CONTENT_AUDIT_HANDLE`
- **路径参数：** `id` - 审核记录ID
- **请求体：**
```json
{
  "auditStatus": 1,
  "auditReason": "内容质量较高，审核通过",
  "auditBy": 100
}
```
- **说明：** `auditStatus`: 0-待审核, 1-通过, 2-不通过
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

##### 2. 获取待审核列表
- **接口：** `GET /api/v1/content/audits/pending`
- **描述：** 分页获取待审核的内容列表
- **权限：** `CONTENT_AUDIT_VIEW`
- **查询参数：**
  - `page` - 页码（必填）
  - `pageSize` - 每页大小（必填）
  - `targetType` - 目标类型（可选）
  - `auditStatus` - 审核状态（可选）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 30,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 3. 获取审核记录
- **接口：** `GET /api/v1/content/audits`
- **描述：** 根据目标对象ID和类型获取审核记录详情
- **权限：** `CONTENT_AUDIT_VIEW`
- **查询参数：**
  - `targetId` - 目标对象ID（必填）
  - `targetType` - 目标对象类型（必填）
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "targetId": 1,
    "targetType": 0,
    "auditStatus": 1,
    "auditReason": "内容质量较高，审核通过",
    "auditBy": 100,
    "createTime": "2024-05-20T10:00:00"
  }
}
```

---

### 字典业务控制器

> 字典业务控制器，用于字典表的增删改查

#### 一、分类控制器 (CategoriesController)

**基础路径：** `/api/v1/content/categories`

##### 1. 创建分类
- **接口：** `POST /api/v1/content/categories`
- **权限：** `CONTENT_CATEGORY_CREATE`
- **请求体：**
```json
{
  "name": "技术",
  "code": "tech",
  "parentId": null,
  "status": 1
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "技术",
    "code": "tech",
    "parentId": null,
    "status": 1,
    "createTime": "2024-05-18T10:00:00"
  }
}
```

##### 2. 更新分类
- **接口：** `PUT /api/v1/content/categories`
- **权限：** `CONTENT_CATEGORY_EDIT`
- **请求体：**
```json
{
  "id": 1,
  "name": "技术分享",
  "code": "tech",
  "parentId": null,
  "status": 1
}
```
- **响应：** 同创建分类响应

##### 3. 删除分类
- **接口：** `DELETE /api/v1/content/categories/{categoryId}`
- **权限：** `CONTENT_CATEGORY_DELETE`
- **路径参数：** `categoryId` - 分类ID
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

##### 4. 分页查询分类列表
- **接口：** `POST /api/v1/content/categories/page`
- **权限：** `CONTENT_CATEGORY_VIEW`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "name": "技术",
    "status": 1,
    "parentId": null
  },
  "keyword": "技术"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 20,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

##### 5. 查询分类详情
- **接口：** `GET /api/v1/content/categories/detail/{categoryId}`
- **权限：** `CONTENT_CATEGORY_VIEW`
- **路径参数：** `categoryId` - 分类ID
- **响应：** 同创建分类响应

##### 6. 启用/禁用分类
- **接口：** `POST /api/v1/content/categories/toggle-enable`
- **权限：** `CONTENT_CATEGORY_TOGGLE`
- **请求体：**
```json
{
  "id": 1,
  "enable": true
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

#### 二、标签控制器 (TagsController)

**基础路径：** `/api/v1/content/tags`

##### 1. 创建标签
- **接口：** `POST /api/v1/content/tags`
- **权限：** `CONTENT_TAG_CREATE`
- **请求体：**
```json
{
  "name": "SpringBoot",
  "description": "与SpringBoot相关"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "SpringBoot",
    "description": "与SpringBoot相关",
    "createTime": "2024-05-18T10:00:00"
  }
}
```

##### 2. 更新标签
- **接口：** `PUT /api/v1/content/tags/{id}`
- **权限：** `CONTENT_TAG_EDIT`
- **路径参数：** `id` - 标签ID
- **请求体：**
```json
{
  "name": "Spring Boot",
  "description": "Spring Boot框架相关内容"
}
```
- **响应：** 同创建标签响应

##### 3. 删除标签
- **接口：** `DELETE /api/v1/content/tags/{id}`
- **权限：** `CONTENT_TAG_DELETE`
- **路径参数：** `id` - 标签ID
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

##### 4. 分页查询标签列表
- **接口：** `POST /api/v1/content/tags/page`
- **权限：** `CONTENT_TAG_VIEW`
- **请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "params": {
    "name": "Spring"
  },
  "keyword": "Spring"
}
```
- **响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 数据模型说明

### 内容类型 (type)
- `0` - 文章
- `1` - 笔记
- `2` - 提问
- `3` - 文件
- `4` - 视频

### 内容状态 (status)
- `0` - 草稿
- `1` - 待审核
- `2` - 已发布
- `3` - 已删除

### 分类状态 (status)
- `0` - 禁用
- `1` - 启用
- `2` - 已删除

### 评论状态 (status)
- `0` - 待审核
- `1` - 已发布
- `2` - 已删除

### 审核状态 (auditStatus)
- `0` - 待审核
- `1` - 通过
- `2` - 不通过

### 目标类型 (targetType)
- `0` - content（内容）
- `1` - comment（评论）
- `2` - user（用户）

### 行为类型 (actionType)
- `0` - view（浏览）
- `1` - like（点赞）
- `2` - favorite（收藏）
- `3` - report（举报）

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 通用数据结构说明

### 分页请求 (PageDTO)
```json
{
  "pageNum": 1,           // 页码，从1开始，必填
  "pageSize": 10,         // 每页条数，必填
  "params": {},           // 查询条件对象，可选
  "keyword": ""           // 关键字搜索，可选
}
```

### 分页响应 (PageVO)
```json
{
  "total": 100,           // 总条数
  "pageNum": 1,           // 当前页
  "pageSize": 10,         // 每页条数
  "records": []           // 当前页数据列表
}
```

### 统一响应 (Result)
```json
{
  "code": 200,            // 状态码
  "message": "success",   // 消息
  "data": {}              // 数据
}
```

---

## 接口设计原则

### 分页 vs 列表查询

本服务接口遵循以下设计原则：

1. **字典数据（不分页）**
   - 标签、分类等字典表
   - 特点：数据量相对较小，变化频率低
   - 接口：`GET /api/v1/content/{resource}` 返回完整列表
   - 用途：下拉选择、前端缓存

2. **业务数据（必须分页）**
   - 内容、评论、审核记录等核心业务数据
   - 特点：数据量大，持续增长
   - 接口：`POST /api/v1/content/{resource}/page` 返回分页数据
   - 用途：列表展示、数据管理

---

## 注意事项

1. 所有需要权限的接口都需要在请求头中携带有效的 JWT Token
2. **分页参数：** 使用 `pageNum`（页码）和 `pageSize`（每页条数），默认每页10条
3. **分页查询：** 查询条件放在 `params` 对象中，支持 `keyword` 关键字搜索
4. 日期格式统一使用 ISO 8601 格式：`yyyy-MM-dd` 或 `yyyy-MM-ddTHH:mm:ss`
5. 文件相关字段（如coverUrl、videoUrl）需要先通过文件服务上传获取
6. **内容发布流程：** 创建内容 → 待审核 → 审核通过 → 已发布
7. **评论审核：** 评论创建后默认为待审核状态，需要管理员审核后才能显示
8. **用户行为记录：** 点赞、收藏等行为会自动更新statistics表的统计数据
9. **删除操作：** 支持逻辑删除（移至垃圾箱）和物理删除两种方式
10. **分类树形结构：** 通过parentId字段实现父子分类关系，支持多级分类
11. **标签关联：** 一个内容可以关联多个标签，通过tag_relation表维护关系
12. **统计数据：** 浏览、点赞、收藏、评论等统计数据实时更新

---

## 更新日志

### v1.0 (2024-05-18)
- ✅ 实现内容发布、编辑、删除、查询功能
- ✅ 支持文章、笔记、提问、文件、视频五种内容类型
- ✅ 实现分类管理功能，支持树形结构
- ✅ 实现标签管理功能，支持多对多关联
- ✅ 实现评论功能，支持父子评论
- ✅ 实现用户交互功能（点赞、收藏、浏览等）
- ✅ 实现审核功能，支持内容和评论审核
- ✅ 实现统计功能，记录浏览、点赞、收藏、评论数
- ✅ 实现热门内容查询功能
