package com.whkj.rocketmq.base;

import com.google.gson.Gson;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import static com.whkj.rocketmq.util.TopicEventMetaDataUtil.getTag;
import static com.whkj.rocketmq.util.TopicEventMetaDataUtil.getTopic;

/**
 * Created by linan on 2018/12/25.
 */
public class MessageConverter {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageConverter.class);
    private static final String EVENT_TYPE = "_eventCls";
    private final static String charset = "UTF-8";
    private static Gson gson = new Gson();

    public static Message convertToRocketMessage(MqEvent event) {
        String topic = getTopic(event);
        String messageBody = gson.toJson(event);
        byte[] payloads = messageBody.getBytes(Charset.forName(charset));

        Message rocketMsg = new Message(topic, payloads);
        rocketMsg.setTags(getTag(event));
        rocketMsg.putUserProperty(EVENT_TYPE, event.getClass().getName());
        return rocketMsg;
    }

    public static MqEvent convertToMqEvent(MessageExt messageExt) {
        String eventClassStr = messageExt.getUserProperty(EVENT_TYPE);
        Class eventClass = getClassFromStr(eventClassStr);
        String jsonStr = new String(messageExt.getBody());
        return (MqEvent) gson.fromJson(jsonStr, eventClass);
    }

    private static Class getClassFromStr(String eventClassStr) {
        try {
            return Class.forName(eventClassStr);
        } catch (ClassNotFoundException e) {
            LOGGER.error("failed to find class {}", eventClassStr);
            throw new RuntimeException(e);
        }
    }
}
