package com.whkj.rocketmq.util;

import com.whkj.rocketmq.TestEvents;
import com.whkj.rocketmq.annotation.Topic;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TopicEventMetaDataUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getTopic() {
        {
            String topic = TopicEventMetaDataUtil.getTopic(TestEvents.AddedTestEvent.class);
            Assert.assertEquals("test_agg_topic", topic);
        }

        {
            String topic = TopicEventMetaDataUtil.getTopic(TestEvents.RemovedTestEvent.class);
            Assert.assertEquals("test_agg_topic", topic);
        }

        {
            String topic = TopicEventMetaDataUtil.getTopic(TestEvents.Removed2TestEvent.class);
            Assert.assertEquals("test_agg_topic_2", topic);
        }

        {
            String topic = TopicEventMetaDataUtil.getTopic(TestEvents.OutterEvent.class);
            Assert.assertEquals("outter_topic", topic);
        }
    }

}
