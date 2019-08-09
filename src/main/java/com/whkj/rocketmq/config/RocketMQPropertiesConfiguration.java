package com.whkj.rocketmq.config;

import com.whkj.rocketmq.base.RocketMQProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Created by linan on 2018/12/29.
 */

public class RocketMQPropertiesConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.rocketmq")
    public RocketMQProperties rocketMQProperties(){
        return new RocketMQProperties();
    }
}
