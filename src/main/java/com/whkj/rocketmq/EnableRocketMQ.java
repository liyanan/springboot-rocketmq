package com.whkj.rocketmq;

import com.whkj.rocketmq.config.RocketMQConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by linan on 2018/12/29.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RocketMQConfigurationSelector.class)
public @interface EnableRocketMQ {
    boolean producer() default true;
    boolean consumer() default true;
}
