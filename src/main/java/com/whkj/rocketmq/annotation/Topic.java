package com.whkj.rocketmq.annotation;


import java.lang.annotation.*;

/**
 * Created by linan on 2018/12/25.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Topic {

    String value();
}
