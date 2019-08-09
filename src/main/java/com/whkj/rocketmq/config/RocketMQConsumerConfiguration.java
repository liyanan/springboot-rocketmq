package com.whkj.rocketmq.config;

import com.whkj.rocketmq.base.RocketMQProperties;
import com.whkj.rocketmq.consumer.ListenerContainerRegister;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Created by linan on 2018/12/29.
 */
public class RocketMQConsumerConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ListenerContainerRegister listenerContainerRegister(RocketMQProperties rocketMQProperties){
        return new ListenerContainerRegister(rocketMQProperties);
    }
}
