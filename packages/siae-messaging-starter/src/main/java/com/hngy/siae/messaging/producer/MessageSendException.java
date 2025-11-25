package com.hngy.siae.messaging.producer;

public class MessageSendException extends RuntimeException {

    public MessageSendException(String message) {
        super(message);
    }

    public MessageSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
