package com.hngy.siae.notification.push;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SsePushServiceImpl implements RealtimePush {

    /** 支持同一用户多端在线：userId -> emitters */
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /** 打开 SSE 连接（供 Controller 调用） */
    public SseEmitter open(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // 不超时，交给网关/Nginx控制
        emitters.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        // 可选：握手确认
        try { emitter.send(SseEmitter.event().name("ready").data(Map.of("ts", Instant.now().toString()))); }
        catch (IOException ignored) {}

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
            if (set.isEmpty()) emitters.remove(userId);
        }
    }
}
