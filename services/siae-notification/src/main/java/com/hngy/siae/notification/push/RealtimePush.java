package com.hngy.siae.notification.push;

/** 抽象实时推送能力；以后切换 WebSocket 只要提供新的 Impl 即可 */
public interface RealtimePush {
    void pushToUser(Long userId, Object payload);
}
