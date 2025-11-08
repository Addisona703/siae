## 🧠 一、Cookie 是什么？

**Cookie（小甜饼）**是一种**存储在浏览器本地的小型键值对数据**，用于**在 HTTP 的无状态通信中维持状态**。

* HTTP 本身是无状态协议：每次请求都是独立的；
* Cookie 就是浏览器和服务器之间共享的“小记事本”，用来标识会话或存储一些用户信息。

> 🌰 比喻：
> 用户访问网站 → 服务器给浏览器发一个“身份牌” → 浏览器保存 → 之后每次访问自动带上这个牌子 → 服务器认得你。

---

## 🧩 二、Cookie 的结构与属性

一个 Cookie 实际上包含以下信息：

| 属性                    | 说明                             |
| --------------------- | ------------------------------ |
| **Name=Value**        | 关键的键值对，例如 `SESSIONID=abc123`   |
| **Domain**            | Cookie 生效的域名（如 `.example.com`） |
| **Path**              | Cookie 生效的路径（如 `/api`）         |
| **Expires / Max-Age** | 过期时间或秒数                        |
| **Secure**            | 仅在 HTTPS 下传输                   |
| **HttpOnly**          | 前端 JS 无法读取（防止 XSS）             |
| **SameSite**          | 限制跨站点请求时是否附带 Cookie（防 CSRF）    |

---

## 🔁 三、Cookie 的生命周期

### 1️⃣ 会话 Cookie（Session Cookie）

* 没有设置 `Expires` 或 `Max-Age`；
* 浏览器关闭后自动清除；
* 适合临时登录状态。

### 2️⃣ 持久 Cookie（Persistent Cookie）

* 设置了 `Expires`（绝对时间）或 `Max-Age`（秒数）；
* 存储在磁盘中，关闭浏览器也会保留；
* 适合“记住我”场景。

---

## 🧭 四、Cookie 的传输流程

1. **服务器设置 Cookie**
   HTTP 响应头中带上：

   ```
   Set-Cookie: SESSIONID=abc123; Path=/; HttpOnly; Secure; SameSite=None
   ```

2. **浏览器保存 Cookie**
   浏览器根据域名、路径、过期时间存起来。

3. **浏览器自动携带 Cookie**
   下次访问匹配的域名+路径时自动附带：

   ```
   Cookie: SESSIONID=abc123
   ```

4. **服务器读取 Cookie**
   服务端根据 Cookie 识别用户身份或状态。

---

## 🌍 五、跨域、子域与 SameSite

### 🔹 SameSite 属性（防 CSRF）

| 值           | 含义                       | 适用场景            |
| ----------- | ------------------------ | --------------- |
| **Strict**  | 严格禁止跨站携带 Cookie          | 高安全、非跨域系统       |
| **Lax**（默认） | GET 链接跳转携带，POST 不带       | 普通系统默认安全        |
| **None**    | 完全允许跨站携带（必须 Secure=true） | 前后端分离登录、第三方跳转回调 |

### 🔹 Domain 属性

| 设置                    | 可访问范围               |
| --------------------- | ------------------- |
| `Domain=example.com`  | `example.com` 和所有子域 |
| `Domain=.example.com` | 同上，更通用              |
| 未设置                   | 仅当前域名有效             |

### 🔹 Path 属性

限定在指定路径下才会携带，例如 `/api`。

---

## 🔐 六、安全性要点（重点！）

| 安全策略                    | 作用                           |
| ----------------------- | ---------------------------- |
| **HttpOnly**            | 禁止 JS 读取 Cookie，防 XSS        |
| **Secure**              | 仅 HTTPS 传输，防中间人窃取            |
| **SameSite=Lax/Strict** | 防跨站伪造请求（CSRF）                |
| **短期有效期 + 续签**          | 减少泄露影响                       |
| **不存敏感数据**              | Cookie 仅存 token 或 session id |
| **Domain / Path 精确控制**  | 避免被不相关页面读取                   |

---

## ⚙️ 七、前后端交互模式

### ✅ 模式 1：**Session Cookie（传统）**

* 服务端生成 sessionId 存在 Cookie；
* 服务端保存 session 数据；
* 简单但扩展性差。

### ✅ 模式 2：**JWT + Cookie（现代混合）**

* 服务端生成 JWT；
* 存在 Cookie 中（HttpOnly + Secure）；
* 无需保存会话，轻量化；
* 可用 Refresh Token 自动续期。

### ✅ 模式 3：**JWT + Header（前后端分离）**

* 前端手动存储 token（localStorage/sessionStorage）；
* 每次请求手动带 `Authorization: Bearer ...`；
* 更灵活，但需要防 XSS。

---

## 🧰 八、服务端常见操作

### 🍪 设置 Cookie

```java
ResponseCookie cookie = ResponseCookie.from("token", token)
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(3600)
    .sameSite("None")
    .build();
response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
```

### ❌ 删除 Cookie

```java
ResponseCookie cookie = ResponseCookie.from("token", "")
    .path("/")
    .maxAge(0)
    .build();
response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
```

### 🧾 读取 Cookie

```java
for (Cookie cookie : request.getCookies()) {
    if ("token".equals(cookie.getName())) {
        return cookie.getValue();
    }
}
```

---

## 💡 九、Cookie 与前端交互

* **默认同源策略**：不同域 Cookie 不共享；
* **跨域请求携带 Cookie**：

    * 前端 axios 要加：

      ```js
      axios.defaults.withCredentials = true;
      ```
    * 服务端要允许：

      ```java
      corsConfig.allowCredentials(true)
                .allowedOrigins("https://frontend.example.com");
      ```

---

## ⚖️ 十、Cookie 与其他存储方式对比

| 存储方式           | 可读性     | 持久性     | 安全性         | 跨域   | 典型用途         |
| -------------- | ------- | ------- | ----------- | ---- | ------------ |
| Cookie         | 浏览器自动发送 | 可设置过期   | 支持 HttpOnly | 有限制  | 登录状态、Session |
| localStorage   | JS 读写   | 持久      | 易受 XSS      | 同源限制 | 保存偏好、缓存数据    |
| sessionStorage | JS 读写   | 关闭标签页清除 | 较安全         | 同源限制 | 页面会话数据       |

---

## 🧭 十一、最佳实践小结

| 场景        | 建议                              |
| --------- | ------------------------------- |
| 登录态管理     | 用 HttpOnly Cookie + JWT         |
| 前后端分离跨域登录 | SameSite=None + Secure=true     |
| 防 XSS     | HttpOnly + 不在 JS 读 Cookie       |
| 防 CSRF    | SameSite=Lax/Strict + Token 双验证 |
| 多子域共享登录   | 设置 Domain=.example.com          |
| 登出        | 删除 Cookie（Path、Domain 一致）       |
| 本地调试      | Secure=false 临时放开               |
