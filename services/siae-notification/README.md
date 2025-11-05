SIAE 通知服务接入指南
================================

本服务负责系统通知的创建、持久化与推送，默认采用 SSE（Server-Sent Events）向前端实时推送消息，并提供 HTTP API 提供 CRUD 能力。本文档覆盖接入方式、配置要点与测试技巧，便于前后端快速落地。

功能概览
--------
- 通知 CRUD：发送、分页查询、标记已读/全部已读、删除。
- 实时推送：SSE 通道按用户 ID 维持长连接，推送 `notify` 事件。
- 统一认证：依赖 `ServiceAuthenticationFilter`，默认仅允许网关或内部服务访问。
- 事件驱动：推荐在事务提交后通过 `@TransactionalEventListener(phase = AFTER_COMMIT)` 触发异步推送，确保只在数据落库成功后推送。

服务接口速览
------------
| 说明 | 方法 | 路径 | 备注 |
| --- | --- | --- | --- |
| 打开 SSE 推送通道 | GET | `/stream?userId={userId}` | `userId` 为监听者 ID；后续所有通知都会推送到该连接。 |
| 发送通知 | POST | `/send` | 需要认证；请求体为 `NotificationCreateDTO`。 |
| 我的通知列表 | GET | `/my` | 支持分页、是否已读过滤。 |
| 未读数量 | GET | `/unread-count` | 返回当前用户未读条数。 |
| 标记已读 | PUT | `/{id}/read` | `id` 为通知 ID。 |
| 全部标记已读 | PUT | `/read-all` | 无请求体。 |
| 删除通知 | DELETE | `/{id}` | 删除单条通知。 |

SSE 最小化接入示例
-----------------
```js
const es = new EventSource(`/notification/stream?userId=${userId}`);

es.addEventListener('notify', event => {
  const payload = JSON.parse(event.data);
  console.log('📩 新通知：', payload.title, payload.id);
});

es.onerror = () => console.warn('SSE 断开，浏览器会自动尝试重连');
```

> 建议在页面卸载时调用 `es.close()` 释放连接；如需鉴权，可在反向代理处追加用户上下文。

前端接入流程
------------
以下示例假设前端是基于 Vue 3 + Pinia/Vuex 与 axios。

1. **封装通知 API**
   ```ts
   // api/notification.ts
   import request from '@/utils/request';

   export const fetchNotifications = (params) =>
     request.get('/notification/my', { params });

   export const markAsRead = (id: number) =>
     request.put(`/notification/${id}/read`);

   export const markAllAsRead = () =>
     request.put('/notification/read-all');

   export const deleteNotification = (id: number) =>
     request.delete(`/notification/${id}`);
   ```

2. **集中管理 SSE 连接**
   ```ts
   // stores/notification.ts
   import { defineStore } from 'pinia';

   export const useNotificationStore = defineStore('notification', {
     state: () => ({
       source: null as EventSource | null,
       list: [] as NotificationVO[],
       unread: 0,
     }),
     actions: {
       openStream(userId: number) {
         if (this.source) return;
         this.source = new EventSource(`/notification/stream?userId=${userId}`);

         this.source.addEventListener('notify', (event) => {
           const payload = JSON.parse(event.data);
           this.list.unshift(payload);
           this.unread += 1;
         });

         this.source.onerror = () => {
           console.warn('SSE 断开，等待浏览器自动重连');
         };
       },
       closeStream() {
         this.source?.close();
         this.source = null;
       },
     },
   });
   ```

3. **组件内使用**
   ```vue
   <script setup lang="ts">
   import { onBeforeUnmount, onMounted } from 'vue';
   import { useNotificationStore } from '@/stores/notification';
   import { fetchNotifications, markAsRead } from '@/api/notification';

   const store = useNotificationStore();
   const userId = 1; // 从登录态获取

   onMounted(async () => {
     store.openStream(userId);
     const { data } = await fetchNotifications({ page: 1, size: 10 });
     store.list = data.records;
     store.unread = data.unreadCount;
   });

   onBeforeUnmount(() => store.closeStream());

   const handleRead = async (id: number) => {
     await markAsRead(id);
     store.unread = Math.max(0, store.unread - 1);
   };
   </script>
   ```

4. **处理鉴权**
   - 通过前端路由守卫确保用户登录态；在请求拦截器为所有 API 调用附加网关要求的头部（如 JWT）。
   - 真正的鉴权通常在 API Gateway 中完成，前端只需保持接口路径与网关对齐即可。

5. **断线重连与兜底**
   - 浏览器会自动重连 SSE；若需要自定义策略，可在 `es.onerror` 中调用 `close()` 并基于指数退避手动重开。
   - 为防止浏览器因网络波动错过消息，可在重新连接后主动调用 `fetchNotifications` 补齐本地状态。

事务内发送注意事项
------------------
若通知发送依赖数据库事务，请使用：
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```
保证只有当事务真正提交后才会执行推送逻辑，避免回滚时向外推送无效数据。

安全接入与网关要求
------------------
服务依赖 `siae-security-starter` 中的 `ServiceAuthenticationFilter`。生产环境默认拒绝直接外部访问，只接受两种来源：

1. 网关转发：需携带 `X-Gateway-Auth: true` 及合法的 `X-Gateway-Secret`。
2. 内部服务调用：需携带头 `X-Internal-Service-Call`，数值等于 `siae.auth.internal-secret-key` 配置项。

若是测试或编排脚本，请额外提供 `X-Caller-Service` 说明调用来源，便于日志排查。

本地/测试环境技巧
-----------------
- **运行集成测试**：示例测试 `NotifyControllerTest` 使用 `TestRestTemplate` 携带内部调用头：
  ```java
  var entity = new HttpEntity<Void>(headers); // headers 包含 X-Internal-Service-Call
  var response = restTemplate.postForEntity("/_test/notify?...",
                                           entity,
                                           Long.class);
  ```
  如需本地直接访问，可在 `application-test.yaml` 或注解参数中开启 `siae.auth.enable-direct-access=true`。
- **推送异步化**：可在推送实现上添加 `@Async`，防止阻塞业务线程。
- **配置加载**：生产配置由 Nacos 下发；本地调试默认读取 `bootstrap.yaml` (`spring.profiles.active=dev`) 并共享 `siae-common.yaml`。

常见问题
--------
- **SSE 连接收不到消息**：确认 `userId` 是否正确、推送逻辑是否在事务提交后执行，以及连接是否被网关或代理劫持。
- **请求被拒绝，提示 “Direct access denied”**：说明缺少网关或内部调用头；请通过网关访问或添加 `X-Internal-Service-Call`。
- **推送阻塞业务线程**：在推送方法上增加 `@Async`，或交由消息队列/事件总线处理。

如需进一步扩展或调试说明，可在仓库 `.specs/messaging-starter` 中查看设计文档。欢迎在提交前补充使用场景与约束条件。 
