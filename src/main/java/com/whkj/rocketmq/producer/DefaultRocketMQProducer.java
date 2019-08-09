package com.whkj.rocketmq.producer;

import com.whkj.rocketmq.base.MessageConverter;
import com.whkj.rocketmq.base.MqEvent;
import com.whkj.rocketmq.base.ShardingKey;
import com.whkj.rocketmq.base.RocketMQProperties;
import jodd.bean.BeanCopy;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.StopWatch;


/**
 * Created by linan on 2018/12/25.
 */
public class DefaultRocketMQProducer implements RocketMQProducer, SmartLifecycle {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultRocketMQProducer.class);

    private volatile boolean isRunning = false;

    private final String producerGroup;

    private final RocketMQProperties rocketMQProperties;

    private DefaultMQProducer producer;

    public DefaultRocketMQProducer(String producerGroup, RocketMQProperties rocketMQProperties) {
        this.producerGroup = producerGroup;
        this.rocketMQProperties = rocketMQProperties;
        this.initProducer();
    }

    @Override
    public SendResult sendMessage(MqEvent event) {
        return sendMessage(event, producer.getSendMsgTimeout());
    }

    @Override
    public SendResult sendMessage(MqEvent event, long timeout) {
        return sendMessage(event, timeout, 0);
    }

    @Override
    public SendResult sendMessage(MqEvent event, long timeout, int delayLevel) {

        SendResult sendResult = null;
        Message rocketMsg = MessageConverter.convertToRocketMessage(event);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            if (delayLevel > 0) {
                rocketMsg.setDelayTimeLevel(delayLevel);
            }
            if (event instanceof ShardingKey) {
                MessageQueueSelector messageQueueSelector = new SelectMessageQueueByHash();
                sendResult = producer.send(rocketMsg, messageQueueSelector, ((ShardingKey) event).getShardingKey(), timeout);
            } else {
                sendResult = producer.send(rocketMsg, timeout);
            }
            stopWatch.stop();
            LOGGER.debug("send message cost: {} ms, msgId:{}", stopWatch.getTotalTimeSeconds(), sendResult.getMsgId());

        } catch (Exception e) {
            LOGGER.error("syncSend failed. destination:{}, message:{}, error:{} ", rocketMsg.getTopic(), rocketMsg, e.toString(), e);
        }
        return sendResult;
    }

    private void initProducer() {
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(rocketMQProperties.getNameServer());
        producer.setVipChannelEnabled(false);
        BeanCopy.beans(rocketMQProperties, producer).copy();
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        stop();
    }

    @Override
    public synchronized void start() {
        if (!isRunning) {
            this.isRunning = true;
            try {
                this.producer.start();
            } catch (MQClientException e) {
                LOGGER.error("failed to start rocketmq producer");
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            this.producer.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
