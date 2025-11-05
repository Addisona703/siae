package com.hngy.siae.messaging.autoconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.amqp.core.Binding;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "siae.messaging.rabbit")
public class SiaeRabbitProperties {

    /**
     * 总开关，默认开启 Starter 自动配置。
     */
    private boolean enabled = true;

    @NotNull
    private final Connection connection = new Connection();

    @NotNull
    private final Publisher publisher = new Publisher();

    @NotNull
    private final Consumer consumer = new Consumer();

    @NotNull
    private final Map<String, Exchange> exchanges = new LinkedHashMap<>();

    @NotNull
    private final Map<String, Queue> queues = new LinkedHashMap<>();

    @NotNull
    private final Map<String, BindingProperties> bindings = new LinkedHashMap<>();

    @NotNull
    private final Map<String, Instance> instances = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Connection getConnection() {
        return connection;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public Map<String, Exchange> getExchanges() {
        return exchanges;
    }

    public Map<String, Queue> getQueues() {
        return queues;
    }

    public Map<String, BindingProperties> getBindings() {
        return bindings;
    }

    public Map<String, Instance> getInstances() {
        return instances;
    }

    @Validated
    public static class Connection {

        /**
         * 连接地址，支持 host:port 或逗号分隔的多节点。
         */
        @NotBlank
        private String addresses = "localhost:5672";

        private String username = "guest";

        private String password = "guest";

        private String virtualHost = "/";

        /**
         * 心跳秒数。
         */
        private int requestedHeartbeat = 30;

        /**
         * 是否启用 SSL。
         */
        private boolean sslEnabled = false;

        public String getAddresses() {
            return addresses;
        }

        public void setAddresses(String addresses) {
            this.addresses = addresses;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getVirtualHost() {
            return virtualHost;
        }

        public void setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
        }

        public int getRequestedHeartbeat() {
            return requestedHeartbeat;
        }

        public void setRequestedHeartbeat(int requestedHeartbeat) {
            this.requestedHeartbeat = requestedHeartbeat;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public void setSslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
        }
    }

    @Validated
    public static class Publisher {

        /**
         * 是否开启 publisher confirm。
         */
        private boolean confirms = true;

        /**
         * 是否开启 return 回调。
         */
        private boolean returns = true;

        @NotNull
        private final Retry retry = new Retry();

        public boolean isConfirms() {
            return confirms;
        }

        public void setConfirms(boolean confirms) {
            this.confirms = confirms;
        }

        public boolean isReturns() {
            return returns;
        }

        public void setReturns(boolean returns) {
            this.returns = returns;
        }

        public Retry getRetry() {
            return retry;
        }

        @Validated
        public static class Retry {

            private boolean enabled = true;

            private int maxAttempts = 3;

            private long initialInterval = 500L;

            private double multiplier = 2.0d;

            private long maxInterval = 5000L;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public long getInitialInterval() {
                return initialInterval;
            }

            public void setInitialInterval(long initialInterval) {
                this.initialInterval = initialInterval;
            }

            public double getMultiplier() {
                return multiplier;
            }

            public void setMultiplier(double multiplier) {
                this.multiplier = multiplier;
            }

            public long getMaxInterval() {
                return maxInterval;
            }

            public void setMaxInterval(long maxInterval) {
                this.maxInterval = maxInterval;
            }
        }
    }

    @Validated
    public static class Consumer {

        /**
         * 默认并发消费者数量。
         */
        private int concurrency = 3;

        /**
         * 最大并发消费者数量。
         */
        private int maxConcurrency = 10;

        /**
         * 每次预取数量。
         */
        private int prefetch = 50;

        /**
         * 默认确认模式。
         */
        private org.springframework.amqp.core.AcknowledgeMode acknowledgeMode =
                org.springframework.amqp.core.AcknowledgeMode.MANUAL;

        @NotNull
        private final Retry retry = new Retry();

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public int getMaxConcurrency() {
            return maxConcurrency;
        }

        public void setMaxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
        }

        public int getPrefetch() {
            return prefetch;
        }

        public void setPrefetch(int prefetch) {
            this.prefetch = prefetch;
        }

        public org.springframework.amqp.core.AcknowledgeMode getAcknowledgeMode() {
            return acknowledgeMode;
        }

        public void setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode acknowledgeMode) {
            this.acknowledgeMode = acknowledgeMode;
        }

        public Retry getRetry() {
            return retry;
        }

        @Validated
        public static class Retry {

            private boolean enabled = true;

            private int maxAttempts = 3;

            private long initialInterval = 1000L;

            private double multiplier = 2.0d;

            private long maxInterval = 10000L;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public long getInitialInterval() {
                return initialInterval;
            }

            public void setInitialInterval(long initialInterval) {
                this.initialInterval = initialInterval;
            }

            public double getMultiplier() {
                return multiplier;
            }

            public void setMultiplier(double multiplier) {
                this.multiplier = multiplier;
            }

            public long getMaxInterval() {
                return maxInterval;
            }

            public void setMaxInterval(long maxInterval) {
                this.maxInterval = maxInterval;
            }
        }
    }

    @Validated
    public static class Exchange {

        @NotBlank
        private String type = "topic";

        private boolean durable = true;

        private boolean autoDelete = false;

        private boolean internal = false;

        private boolean delayed = false;

        private Map<String, Object> arguments = new LinkedHashMap<>();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isDurable() {
            return durable;
        }

        public void setDurable(boolean durable) {
            this.durable = durable;
        }

        public boolean isAutoDelete() {
            return autoDelete;
        }

        public void setAutoDelete(boolean autoDelete) {
            this.autoDelete = autoDelete;
        }

        public boolean isInternal() {
            return internal;
        }

        public void setInternal(boolean internal) {
            this.internal = internal;
        }

        public boolean isDelayed() {
            return delayed;
        }

        public void setDelayed(boolean delayed) {
            this.delayed = delayed;
        }

        public Map<String, Object> getArguments() {
            return arguments;
        }

        public void setArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }
    }

    @Validated
    public static class Queue {

        private boolean durable = true;

        private boolean exclusive = false;

        private boolean autoDelete = false;

        private Map<String, Object> arguments = new LinkedHashMap<>();

        public boolean isDurable() {
            return durable;
        }

        public void setDurable(boolean durable) {
            this.durable = durable;
        }

        public boolean isExclusive() {
            return exclusive;
        }

        public void setExclusive(boolean exclusive) {
            this.exclusive = exclusive;
        }

        public boolean isAutoDelete() {
            return autoDelete;
        }

        public void setAutoDelete(boolean autoDelete) {
            this.autoDelete = autoDelete;
        }

        public Map<String, Object> getArguments() {
            return arguments;
        }

        public void setArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }
    }

    @Validated
    public static class BindingProperties {

        @NotBlank
        private String exchange;

        @NotBlank
        private String destination;

        private Binding.DestinationType destinationType = Binding.DestinationType.QUEUE;

        private String routingKey = "";

        private Map<String, Object> arguments = new LinkedHashMap<>();

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public Binding.DestinationType getDestinationType() {
            return destinationType;
        }

        public void setDestinationType(Binding.DestinationType destinationType) {
            this.destinationType = destinationType;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public Map<String, Object> getArguments() {
            return arguments;
        }

        public void setArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }
    }

    @Validated
    public static class Instance {

        @NotNull
        private final Connection connection = new Connection();

        @NotNull
        private final Publisher publisher = new Publisher();

        @NotNull
        private final Consumer consumer = new Consumer();

        @NotNull
        private final Map<String, Exchange> exchanges = new LinkedHashMap<>();

        @NotNull
        private final Map<String, Queue> queues = new LinkedHashMap<>();

        @NotNull
        private final Map<String, BindingProperties> bindings = new LinkedHashMap<>();

        public Connection getConnection() {
            return connection;
        }

        public Publisher getPublisher() {
            return publisher;
        }

        public Consumer getConsumer() {
            return consumer;
        }

        public Map<String, Exchange> getExchanges() {
            return exchanges;
        }

        public Map<String, Queue> getQueues() {
            return queues;
        }

        public Map<String, BindingProperties> getBindings() {
            return bindings;
        }
    }
}
