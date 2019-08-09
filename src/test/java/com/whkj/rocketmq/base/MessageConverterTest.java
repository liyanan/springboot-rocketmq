package com.whkj.rocketmq.base;

import com.whkj.rocketmq.TestEvents;
import jodd.bean.BeanCopy;
import org.apache.commons.lang3.RandomUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageConverterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void convert() {
        TestEvents.AddedTestEvent addedTestEvent = new TestEvents.AddedTestEvent(RandomUtils.nextLong(), String.valueOf(RandomUtils.nextLong()));
        Message message = MessageConverter.convertToRocketMessage(addedTestEvent);
        MessageExt messageExt = new MessageExt();
        messageExt.setBody(message.getBody());
        messageExt.setTopic(message.getTopic());
        messageExt.setFlag(message.getFlag());
        messageExt.putUserProperty("_eventCls", message.getUserProperty("_eventCls"));
        TestEvents.AddedTestEvent addedTestEvent1 = (TestEvents.AddedTestEvent) MessageConverter.convertToMqEvent(messageExt);
        Assert.assertEquals(addedTestEvent, addedTestEvent1);

    }
}
