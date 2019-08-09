package com.whkj.rocketmq.producer;

import com.whkj.rocketmq.base.MqEvent;
import org.apache.rocketmq.client.producer.SendResult;

/**
 * Created by linan on 2018/12/24.
 */
public interface RocketMQProducer {

    /**
     * @param event 待发送消息体
     * @return
     */
    SendResult sendMessage(MqEvent event);

    /**
     * @param event   待发送消息体
     * @param timeout 超时时间
     * @return
     */
    SendResult sendMessage(MqEvent event, long timeout);

    /**
     * @param event      待发送消息体
     * @param timeout    超时时间
     * @param delayLevel 延迟发送时间
     * @return
     */
    SendResult sendMessage(MqEvent event, long timeout, int delayLevel);

}
