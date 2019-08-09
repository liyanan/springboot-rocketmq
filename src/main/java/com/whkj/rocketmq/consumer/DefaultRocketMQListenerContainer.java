package com.whkj.rocketmq.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.util.Objects;

public class DefaultRocketMQListenerContainer implements InitializingBean, DisposableBean, SmartLifecycle {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultRocketMQListenerContainer.class);

    private long suspendCurrentQueueTimeMillis = 1000;

    private int delayLevelWhenNextConsume = 0;

    private String nameServer;

    private String consumerGroup;

    private String topic;

    private String tag = "*";

    private int consumeThreadMax = 64;

    private DefaultMQPushConsumer consumer;

    private boolean running;

    private ConsumeMode consumeMode;

    private MessageModel messageModel;

    private MessageCallback messageCallback;


    public DefaultRocketMQListenerContainer nameServer(String nameServer) {
        this.nameServer = nameServer;
        return this;
    }

    public DefaultRocketMQListenerContainer consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    public DefaultRocketMQListenerContainer topic(String topic) {
        this.topic = topic;
        return this;
    }

    public DefaultRocketMQListenerContainer consumeModel(ConsumeMode consumeMode) {
        this.consumeMode = consumeMode;
        return this;
    }

    public DefaultRocketMQListenerContainer messageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
        return this;
    }

    public DefaultRocketMQListenerContainer tag(String tag) {
        this.tag = tag;
        return this;
    }

    public DefaultRocketMQListenerContainer messageCallback(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
        return this;
    }

    @Override
    public void destroy() {
        this.setRunning(false);
        if (Objects.nonNull(consumer)) {
            consumer.shutdown();
        }
        LOGGER.info("container destroyed, {}", this.toString());
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("container already running. " + this.toString());
        }

        try {
            consumer.start();
        } catch (MQClientException e) {
            throw new IllegalStateException("Failed to start RocketMQ push consumer", e);
        }
        this.setRunning(true);

        LOGGER.info("running container: {}", this.toString());
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            if (Objects.nonNull(consumer)) {
                consumer.shutdown();
            }
            setRunning(false);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initRocketMQPushConsumer();
    }

    @Override
    public String toString() {
        return "DefaultRocketMQListenerContainer{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", nameServer='" + nameServer + '\'' +
                ", topic='" + topic + '\'' +
                ", consumeMode=" + consumeMode +
                ", selectorType=TAG" +
                ", messageModel=" + messageModel +
                '}';
    }

    private void initRocketMQPushConsumer() throws MQClientException {
        Assert.notNull(consumerGroup, "Property 'consumerGroup' is required");
        Assert.notNull(nameServer, "Property 'nameServer' is required");
        Assert.notNull(topic, "Property 'topic' is required");
        Assert.notNull(tag, "tag is required");
        Assert.notNull(this.messageCallback, "Message Callback can not be null");

        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setVipChannelEnabled(false);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.setMessageModel(messageModel);
        consumer.subscribe(topic, tag);
        if (consumeThreadMax < consumer.getConsumeThreadMin()) {
            consumer.setConsumeThreadMin(consumeThreadMax);
        }

        switch (consumeMode) {
            case CONCURRENTLY:
                registerConcurrentlyMessageListener();
                break;
            case ORDERLY:
                registerOrderlyMessageListener();
                break;
            default:
                throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
        }

    }

    private void registerConcurrentlyMessageListener() {
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt messageExt : msgs) {
                LOGGER.debug("received msg: {}", messageExt);
                try {
                    handleMessage(messageExt);
                } catch (Exception e) {
                    LOGGER.warn("consume message failed. messageExt:{}", messageExt, e);
                    context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
    }

    private void registerOrderlyMessageListener() {
        consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
            for (MessageExt messageExt : msgs) {
                LOGGER.debug("received msg: {}", messageExt);
                try {
                    handleMessage(messageExt);
                } catch (Exception e) {
                    LOGGER.warn("consume message failed. messageExt:{}", messageExt, e);
                    context.setSuspendCurrentQueueTimeMillis(suspendCurrentQueueTimeMillis);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }

            return ConsumeOrderlyStatus.SUCCESS;
        });
    }

    private void handleMessage(MessageExt messageExt) throws Exception {
        long now = System.currentTimeMillis();
        this.messageCallback.onMessage(messageExt);
        long costTime = System.currentTimeMillis() - now;
        LOGGER.info("consume {} cost: {} ms", messageExt.getMsgId(), costTime);
    }

    public interface MessageCallback {
        void onMessage(MessageExt messageExt);
    }
}
