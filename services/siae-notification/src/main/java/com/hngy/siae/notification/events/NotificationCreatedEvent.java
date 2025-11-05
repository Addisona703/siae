package com.hngy.siae.notification.events;

import com.hngy.siae.notification.entity.SystemNotification;

public record NotificationCreatedEvent(SystemNotification notification) {}
