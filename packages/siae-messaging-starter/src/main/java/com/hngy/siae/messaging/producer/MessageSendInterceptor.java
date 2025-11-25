package com.hngy.siae.messaging.producer;

public interface MessageSendInterceptor {

    default void beforeSend(MessageSendContext context) {
    }

    default void afterSend(MessageSendContext context) {
    }

    default void onError(MessageSendContext context, Throwable throwable) {
    }
}
