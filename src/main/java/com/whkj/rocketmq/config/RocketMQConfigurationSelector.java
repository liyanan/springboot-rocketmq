package com.whkj.rocketmq.config;

import com.google.common.collect.Sets;
import com.whkj.rocketmq.EnableRocketMQ;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

/**
 * Created by linan on 2018/12/29.
 */
public class RocketMQConfigurationSelector implements ImportSelector{

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Set<String> configurations = Sets.newHashSet();
        configurations.add(RocketMQPropertiesConfiguration.class.getName());
        boolean isProducer = getConfigValue(importingClassMetadata, "producer");
        boolean isConsumer = getConfigValue(importingClassMetadata, "consumer");
        if (isProducer){
            configurations.add(RocketMQProducerConfiguration.class.getName());
        }
        if (isConsumer){
            configurations.add(RocketMQConsumerConfiguration.class.getName());
        }
        return configurations.toArray(new String[configurations.size()]);
    }

    private boolean getConfigValue(AnnotationMetadata importingClassMetadata, String key) {
        final Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableRocketMQ.class.getName());
        Object value = annotationAttributes.get(key);
        return (boolean) value;
    }
}
