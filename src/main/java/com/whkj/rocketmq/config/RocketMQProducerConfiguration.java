package com.whkj.rocketmq.config;

import com.whkj.rocketmq.base.RocketMQProperties;
import com.whkj.rocketmq.producer.DefaultRocketMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Created by linan on 2018/12/25.
 */
public class RocketMQProducerConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    @ConditionalOnMissingBean
    public DefaultRocketMQProducer defaultRocketMqProducer(RocketMQProperties rocketMQProperties) {
        return new DefaultRocketMQProducer(getProducerGroupName(), rocketMQProperties);
    }

    protected String getProducerGroupName() {
        return this.applicationName;
    }


}
