package com.hngy.siae.notification.push;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class SsePushServiceImpl implements RealtimePush {

    /** 支持同一用户多端在线：userId -> emitters */
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    
    /** 心跳定时器 */
    private ScheduledExecutorService heartbeatScheduler;
    
    /** 心跳间隔（秒） */
    private static final int HEARTBEAT_INTERVAL = 30;

    @PostConstruct
    public void init() {
        // 启动心跳定时任务
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-heartbeat");
            t.setDaemon(true);
            return t;
        });
        
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, 
                HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
        
        log.info("SSE heartbeat scheduler started with interval: {}s", HEARTBEAT_INTERVAL);
    }
    
    @PreDestroy
    public void destroy() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdown();
            log.info("SSE heartbeat scheduler stopped");
        }
        
        // 关闭所有连接
        emitters.forEach((userId, set) -> {
            set.forEach(emitter -> {
                try {
                    emitter.complete();
                } catch (Exception ignored) {}
            });
        });
        emitters.clear();
    }

    /** 打开 SSE 连接（供 Controller 调用） */
    public SseEmitter open(Long userId) {
        // 设置超时时间为0表示不超时，由心跳机制保持连接
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> {
            remove(userId, emitter);
            log.debug("SSE connection completed for user: {}", userId);
        });
        
        emitter.onTimeout(() -> {
            remove(userId, emitter);
            log.debug("SSE connection timeout for user: {}", userId);
        });
        
        emitter.onError(e -> {
            remove(userId, emitter);
            log.debug("SSE connection error for user: {}, error: {}", userId, e.getMessage());
        });

        // 发送握手确认
        try {
            emitter.send(SseEmitter.event()
                    .name("ready")
                    .data(Map.of(
                            "ts", Instant.now().toString(),
                            "userId", userId,
                            "heartbeatInterval", HEARTBEAT_INTERVAL
                    )));
            log.info("SSE connection established for user: {}, total connections: {}", 
                    userId, getConnectionCount());
        } catch (IOException e) {
            log.warn("Failed to send ready event to user: {}", userId);
            remove(userId, emitter);
        }

        return emitter;
    }

    /** 实时推送给指定用户（业务层只依赖接口，不关心实现） */
    @Override
    public void pushToUser(Long userId, Object payload) {
        Set<SseEmitter> conns = emitters.getOrDefault(userId, Set.of());
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter sse : conns) {
            try { sse.send(SseEmitter.event().name("notify").data(payload)); }
            catch (IOException e) { dead.add(sse); }
        }
        dead.forEach(sse -> remove(userId, sse));
    }

    private void remove(Long userId, SseEmitter emitter) {
        Set<SseEmitter> set = emitters.get(userId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
    
    /** 发送心跳到所有连接 */
    private void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        
        int totalConnections = 0;
        int failedConnections = 0;
        
        for (Map.Entry<Long, Set<SseEmitter>> entry : emitters.entrySet()) {
            Long userId = entry.getKey();
            Set<SseEmitter> conns = entry.getValue();
            List<SseEmitter> dead = new ArrayList<>();
            
            for (SseEmitter sse : conns) {
                totalConnections++;
                try {
                    sse.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(Map.of("ts", Instant.now().toString())));
                } catch (IOException e) {
                    dead.add(sse);
                    failedConnections++;
                }
            }
            
            dead.forEach(sse -> remove(userId, sse));
        }
        
        if (failedConnections > 0) {
            log.debug("Heartbeat sent: {} total, {} failed", totalConnections, failedConnections);
        }
    }
    
    /** 获取当前连接数 */
    public int getConnectionCount() {
        return emitters.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
    
    /** 获取在线用户数 */
    public int getOnlineUserCount() {
        return emitters.size();
    }
    
    /** 检查用户是否在线 */
    public boolean isUserOnline(Long userId) {
        Set<SseEmitter> conns = emitters.get(userId);
        return conns != null && !conns.isEmpty();
    }
}
