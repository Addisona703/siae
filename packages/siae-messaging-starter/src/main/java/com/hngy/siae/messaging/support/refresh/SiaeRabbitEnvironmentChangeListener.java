package com.hngy.siae.messaging.support.refresh;

import com.hngy.siae.messaging.autoconfig.SiaeRabbitProperties;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 监听配置中心刷新事件，动态更新连接参数。
 */
public class SiaeRabbitEnvironmentChangeListener implements ApplicationListener<EnvironmentChangeEvent> {

    private static final String PREFIX = "siae.messaging.rabbit.connection";

    private final Environment environment;
    private final SiaeRabbitConnectionRefresher refresher;

    public SiaeRabbitEnvironmentChangeListener(Environment environment,
                                               SiaeRabbitConnectionRefresher refresher) {
        this.environment = environment;
        this.refresher = refresher;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        boolean connectionChanged = keys.stream().anyMatch(key -> key.startsWith(PREFIX));
        if (!connectionChanged) {
            return;
        }
        Binder binder = Binder.get(environment);
        try {
            SiaeRabbitProperties.Connection updated = binder.bind(PREFIX, Bindable.of(SiaeRabbitProperties.Connection.class))
                    .orElse(null);
            if (updated != null) {
                refresher.refreshIfNecessary(updated);
            }
        } catch (BindException ignore) {
            // ignore bad binding; leave existing connection intact
        }
    }
}
