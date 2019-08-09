package com.whkj.rocketmq.util;

import com.whkj.rocketmq.annotation.Topic;
import com.whkj.rocketmq.base.MqEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by linan on 2018/12/26.
 */
public final class TopicEventMetaDataUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(TopicEventMetaDataUtil.class);

    private static final Map<Class<? extends MqEvent>, String> topicEventMetaDataMap = new ConcurrentHashMap();

    private TopicEventMetaDataUtil() {

    }

    public static String getTag(MqEvent event) {
        return getTag(event.getClass());
    }

    public static String getTag(Class clazz) {
        return clazz.getSimpleName();
    }

    public static String getTopic(Class clazz) {
        return topicEventMetaDataMap.computeIfAbsent(clazz, TopicEventMetaDataUtil::getTopicFromClass);
    }

    private static String getTopicFromClass(Class clazz) {
        Topic topicAnn = (Topic) clazz.getAnnotation(Topic.class);
        if (topicAnn == null) {
            Class enclosingCls = clazz.getEnclosingClass();
            if (enclosingCls != null) {
                topicAnn = (Topic) enclosingCls.getAnnotation(Topic.class);
            }
        }
        if (Objects.isNull(topicAnn) || StringUtils.isBlank(topicAnn.value())) {
            LOGGER.warn("cannot get value annotation info by class:{}", clazz);
            throw new IllegalArgumentException("`message.topic` cannot be null");
        }

        return  topicAnn.value();
    }

    public static String getTopic(MqEvent event) {
        return getTopic(event.getClass());
    }
}
