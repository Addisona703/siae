# OAuth 第三方认证前端使用指南

## 概述

SIAE 认证服务支持多种第三方 OAuth 登录方式，包括：
- QQ 登录
- GitHub 登录
- Gitee 登录

本文档将详细说明前端如何接入和使用第三方认证功能。

---

## 支持的认证流程

系统支持两种认证流程：

### 1. **新用户注册流程（推荐）**
适用于希望新用户完善账号信息后再注册的场景。

### 2. **自动注册流程**
第三方账号首次登录时自动创建系统账号（已废弃，不推荐使用）。

---

## 接口说明

### 基础 URL
```
http://localhost:8000/api/v1/auth/oauth
```

### 1. 发起第三方登录

**接口**: `GET /oauth/login`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| provider | string | 是 | 第三方平台标识：`qq`、`github`、`gitee` |

**请求示例**:
```javascript
// 使用 axios
const response = await axios.get('/api/v1/auth/oauth/login', {
  params: {
    provider: 'github'  // 或 'qq', 'gitee'
  }
});

// 返回示例
{
  "code": 200,
  "message": "success",
  "data": {
    "authUrl": "https://github.com/login/oauth/authorize?client_id=xxx&redirect_uri=xxx&state=xxx"
  }
}
```

**前端处理**:
```javascript
// 获取授权 URL 后，跳转到第三方登录页面
window.location.href = response.data.data.authUrl;
```

---

### 2. 处理授权回调（新版本）

**接口**: `GET /oauth/callback/{provider}`

**说明**:
- 这个接口由后端自动处理第三方平台的回调
- 处理完成后会重定向到前端登录页面，并在 URL 参数中携带结果信息
- **前端无需直接调用此接口**，只需要处理重定向后的 URL 参数

**回调 URL 示例**:

#### 情况 1: 已绑定用户（直接登录）
```
http://localhost:8000/api/v1/auth/login.html?oauth_callback=true&provider=github&need_register=false&access_token=xxx&refresh_token=xxx
```

**URL 参数说明**:
| 参数名 | 类型 | 说明 |
|--------|------|------|
| oauth_callback | boolean | 标识这是 OAuth 回调 |
| provider | string | 第三方平台标识 |
| need_register | boolean | 是否需要注册（false 表示已绑定） |
| access_token | string | 访问令牌 |
| refresh_token | string | 刷新令牌 |

#### 情况 2: 新用户（需要完善信息）
```
http://localhost:8000/api/v1/auth/login.html?oauth_callback=true&provider=github&need_register=true&temp_token=xxx&nickname=xxx&avatar=xxx
```

**URL 参数说明**:
| 参数名 | 类型 | 说明 |
|--------|------|------|
| oauth_callback | boolean | 标识这是 OAuth 回调 |
| provider | string | 第三方平台标识 |
| need_register | boolean | 是否需要注册（true 表示新用户） |
| temp_token | string | 临时令牌（用于后续注册接口） |
| nickname | string | 第三方昵称（URL 编码） |
| avatar | string | 第三方头像 URL（URL 编码） |

---

### 3. 完善信息并注册（新用户）

**接口**: `POST /oauth/register`

**请求头**:
```
Content-Type: application/json
```

**请求参数**:
```javascript
{
  "tempToken": "xxx",           // 必填，从回调 URL 获取的临时令牌
  "username": "myusername",     // 必填，用户名（3-20个字符）
  "email": "user@example.com",  // 可选，邮箱
  "password": "123456"          // 可选，密码（6-20个字符，用于后续账号密码登录）
}
```

**响应示例**:
```javascript
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 123,
    "username": "myusername",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

---

### 4. 绑定第三方账号

**接口**: `POST /oauth/bind`

**说明**: 已登录用户可以绑定第三方账号

**请求头**:
```
Authorization: Bearer {access_token}
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| provider | string | 是 | 第三方平台标识 |

**请求示例**:
```javascript
const response = await axios.post('/api/v1/auth/oauth/bind', null, {
  params: {
    provider: 'github'
  },
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

// 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "authUrl": "https://github.com/login/oauth/authorize?..."
  }
}

// 跳转到授权页面
window.location.href = response.data.data.authUrl;
```

---

### 5. 解绑第三方账号

**接口**: `POST /oauth/unbind`

**请求头**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**请求参数**:
```javascript
{
  "provider": "github"  // 要解绑的第三方平台标识
}
```

**响应示例**:
```javascript
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**注意事项**:
- 不能解绑最后一个登录方式（至少保留一种登录方式）
- 如果只有一个第三方账号且未设置密码，解绑会失败

---

### 6. 查询已绑定的第三方账号

**接口**: `GET /oauth/accounts`

**请求头**:
```
Authorization: Bearer {access_token}
```

**响应示例**:
```javascript
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "provider": "github",
      "nickname": "张三",
      "avatar": "https://avatars.githubusercontent.com/u/123456",
      "createdAt": "2024-01-15 10:30:00"
    },
    {
      "provider": "qq",
      "nickname": "李四",
      "avatar": "http://thirdqq.qlogo.cn/...",
      "createdAt": "2024-01-20 14:20:00"
    }
  ]
}
```

---

## 前端完整实现示例

### Vue 3 示例

```vue
<template>
  <div class="oauth-login">
    <!-- 第三方登录按钮 -->
    <button @click="handleOAuthLogin('github')">
      <img src="@/assets/github-icon.png" alt="GitHub">
      使用 GitHub 登录
    </button>

    <button @click="handleOAuthLogin('qq')">
      <img src="@/assets/qq-icon.png" alt="QQ">
      使用 QQ 登录
    </button>

    <!-- 新用户完善信息表单 -->
    <div v-if="showRegisterForm" class="register-form">
      <h3>完善账号信息</h3>
      <input v-model="registerForm.username" placeholder="请输入用户名（3-20个字符）" />
      <input v-model="registerForm.email" type="email" placeholder="请输入邮箱（可选）" />
      <input v-model="registerForm.password" type="password" placeholder="请设置密码（可选，6-20个字符）" />
      <button @click="handleOAuthRegister">完成注册</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const route = useRoute();

const showRegisterForm = ref(false);
const registerForm = ref({
  tempToken: '',
  username: '',
  email: '',
  password: ''
});

// 1. 发起第三方登录
const handleOAuthLogin = async (provider) => {
  try {
    const response = await axios.get('/api/v1/auth/oauth/login', {
      params: { provider }
    });

    // 跳转到第三方授权页面
    window.location.href = response.data.data.authUrl;
  } catch (error) {
    console.error('发起登录失败:', error);
    alert('登录失败，请稍后重试');
  }
};

// 2. 处理 OAuth 回调
const handleOAuthCallback = () => {
  const query = route.query;

  // 检查是否是 OAuth 回调
  if (query.oauth_callback !== 'true') {
    return;
  }

  const needRegister = query.need_register === 'true';

  if (needRegister) {
    // 新用户，显示注册表单
    showRegisterForm.value = true;
    registerForm.value.tempToken = query.temp_token;
    // 可以预填充昵称作为用户名建议
    registerForm.value.username = decodeURIComponent(query.nickname || '');
  } else {
    // 已绑定用户，直接登录
    const accessToken = query.access_token;
    const refreshToken = query.refresh_token;

    // 保存 token
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    // 跳转到首页
    router.push('/home');
  }
};

// 3. 完成 OAuth 注册
const handleOAuthRegister = async () => {
  try {
    const response = await axios.post('/api/v1/auth/oauth/register', registerForm.value);

    const { accessToken, refreshToken } = response.data.data;

    // 保存 token
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    // 跳转到首页
    router.push('/home');
  } catch (error) {
    console.error('注册失败:', error);
    alert(error.response?.data?.message || '注册失败，请稍后重试');
  }
};

// 页面加载时检查是否是 OAuth 回调
onMounted(() => {
  handleOAuthCallback();
});
</script>
```

### React 示例

```jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';

function OAuthLogin() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [showRegisterForm, setShowRegisterForm] = useState(false);
  const [registerForm, setRegisterForm] = useState({
    tempToken: '',
    username: '',
    email: '',
    password: ''
  });

  // 1. 发起第三方登录
  const handleOAuthLogin = async (provider) => {
    try {
      const response = await axios.get('/api/v1/auth/oauth/login', {
        params: { provider }
      });

      // 跳转到第三方授权页面
      window.location.href = response.data.data.authUrl;
    } catch (error) {
      console.error('发起登录失败:', error);
      alert('登录失败，请稍后重试');
    }
  };

  // 2. 处理 OAuth 回调
  useEffect(() => {
    const oauthCallback = searchParams.get('oauth_callback');

    if (oauthCallback !== 'true') {
      return;
    }

    const needRegister = searchParams.get('need_register') === 'true';

    if (needRegister) {
      // 新用户，显示注册表单
      setShowRegisterForm(true);
      setRegisterForm(prev => ({
        ...prev,
        tempToken: searchParams.get('temp_token'),
        username: decodeURIComponent(searchParams.get('nickname') || '')
      }));
    } else {
      // 已绑定用户，直接登录
      const accessToken = searchParams.get('access_token');
      const refreshToken = searchParams.get('refresh_token');

      // 保存 token
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      // 跳转到首页
      navigate('/home');
    }
  }, [searchParams, navigate]);

  // 3. 完成 OAuth 注册
  const handleOAuthRegister = async () => {
    try {
      const response = await axios.post('/api/v1/auth/oauth/register', registerForm);

      const { accessToken, refreshToken } = response.data.data;

      // 保存 token
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);

      // 跳转到首页
      navigate('/home');
    } catch (error) {
      console.error('注册失败:', error);
      alert(error.response?.data?.message || '注册失败，请稍后重试');
    }
  };

  return (
    <div className="oauth-login">
      {/* 第三方登录按钮 */}
      <button onClick={() => handleOAuthLogin('github')}>
        <img src="/assets/github-icon.png" alt="GitHub" />
        使用 GitHub 登录
      </button>

      <button onClick={() => handleOAuthLogin('qq')}>
        <img src="/assets/qq-icon.png" alt="QQ" />
        使用 QQ 登录
      </button>

      {/* 新用户完善信息表单 */}
      {showRegisterForm && (
        <div className="register-form">
          <h3>完善账号信息</h3>
          <input
            value={registerForm.username}
            onChange={(e) => setRegisterForm({...registerForm, username: e.target.value})}
            placeholder="请输入用户名（3-20个字符）"
          />
          <input
            value={registerForm.email}
            onChange={(e) => setRegisterForm({...registerForm, email: e.target.value})}
            type="email"
            placeholder="请输入邮箱（可选）"
          />
          <input
            value={registerForm.password}
            onChange={(e) => setRegisterForm({...registerForm, password: e.target.value})}
            type="password"
            placeholder="请设置密码（可选，6-20个字符）"
          />
          <button onClick={handleOAuthRegister}>完成注册</button>
        </div>
      )}
    </div>
  );
}

export default OAuthLogin;
```

---

## 用户账号管理示例

### 显示已绑定的第三方账号

```vue
<template>
  <div class="account-bindings">
    <h3>第三方账号绑定</h3>

    <div v-for="account in boundAccounts" :key="account.provider" class="account-item">
      <img :src="getProviderIcon(account.provider)" :alt="account.provider" />
      <span>{{ account.nickname }}</span>
      <span>绑定时间：{{ account.createdAt }}</span>
      <button @click="handleUnbind(account.provider)">解绑</button>
    </div>

    <div class="available-providers">
      <h4>可绑定的第三方账号</h4>
      <button
        v-for="provider in availableProviders"
        :key="provider"
        @click="handleBind(provider)"
      >
        绑定 {{ provider }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const boundAccounts = ref([]);
const allProviders = ['qq', 'github', 'gitee'];

// 获取已绑定账号列表
const fetchBoundAccounts = async () => {
  try {
    const response = await axios.get('/api/v1/auth/oauth/accounts', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    });
    boundAccounts.value = response.data.data;
  } catch (error) {
    console.error('获取绑定账号失败:', error);
  }
};

// 可绑定的第三方账号
const availableProviders = computed(() => {
  const bound = boundAccounts.value.map(a => a.provider);
  return allProviders.filter(p => !bound.includes(p));
});

// 绑定第三方账号
const handleBind = async (provider) => {
  try {
    const response = await axios.post('/api/v1/auth/oauth/bind', null, {
      params: { provider },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    });

    // 跳转到授权页面
    window.location.href = response.data.data.authUrl;
  } catch (error) {
    console.error('绑定失败:', error);
    alert('绑定失败，请稍后重试');
  }
};

// 解绑第三方账号
const handleUnbind = async (provider) => {
  if (!confirm(`确定要解绑 ${provider} 账号吗？`)) {
    return;
  }

  try {
    await axios.post('/api/v1/auth/oauth/unbind',
      { provider },
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        }
      }
    );

    alert('解绑成功');
    // 重新获取绑定列表
    fetchBoundAccounts();
  } catch (error) {
    console.error('解绑失败:', error);
    alert(error.response?.data?.message || '解绑失败，请稍后重试');
  }
};

const getProviderIcon = (provider) => {
  const icons = {
    qq: '/assets/qq-icon.png',
    github: '/assets/github-icon.png',
    gitee: '/assets/gitee-icon.png'
  };
  return icons[provider] || '';
};

onMounted(() => {
  fetchBoundAccounts();
});
</script>
```

---

## 常见问题

### 1. 如何配置第三方应用？

需要在对应的第三方平台创建 OAuth 应用：

- **GitHub**: https://github.com/settings/developers
- **QQ**: https://connect.qq.com/
- **Gitee**: https://gitee.com/oauth/applications

配置回调地址时，使用以下格式：
```
http://localhost:8000/api/v1/auth/oauth/callback/{provider}
```

### 2. State 参数有什么作用？

State 参数用于防止 CSRF 攻击，有效期为 5 分钟（可在配置文件中修改）。

### 3. 临时令牌（tempToken）的有效期是多久？

临时令牌的有效期为 10 分钟，在此期间用户需要完成信息填写并提交注册。

### 4. 用户可以绑定多个第三方账号吗？

可以。一个用户可以同时绑定 QQ、GitHub、Gitee 等多个第三方账号。

### 5. 如何处理错误？

所有接口在失败时会返回标准错误格式：
```javascript
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

常见错误码：
- `400`: 请求参数错误
- `401`: 未授权/Token 无效
- `403`: 权限不足
- `500`: 服务器内部错误

---

## 安全建议

1. **HTTPS**: 生产环境必须使用 HTTPS
2. **Token 存储**: 使用 `httpOnly` Cookie 或安全的本地存储
3. **Token 过期处理**: 实现自动刷新 Token 机制
4. **CORS 配置**: 正确配置跨域策略
5. **敏感信息**: 不要在前端代码中硬编码任何密钥

---

## 附录：完整流程图

```
用户点击第三方登录
    ↓
前端调用 /oauth/login 获取授权 URL
    ↓
跳转到第三方授权页面
    ↓
用户授权
    ↓
第三方平台回调后端 /oauth/callback/{provider}
    ↓
后端处理并重定向到前端页面（携带参数）
    ↓
前端解析 URL 参数
    ├─→ need_register=false → 直接登录（保存 token）
    └─→ need_register=true → 显示注册表单
            ↓
        用户填写信息
            ↓
        前端调用 /oauth/register
            ↓
        后端创建用户并返回 token
            ↓
        前端保存 token 并跳转
```

---

## 更新日志

- **2024-01**: 初始版本，支持 QQ、GitHub、Gitee 登录
- **2024-01**: 新增账号绑定/解绑功能
- **2024-01**: 优化新用户注册流程

---

## 技术支持

如有问题，请联系开发团队或提交 Issue。
