package com.whkj.rocketmq.test;

import com.google.common.collect.Sets;
import com.whkj.rocketmq.EnableRocketMQ;
import com.whkj.rocketmq.TestEvents;
import com.whkj.rocketmq.annotation.HandlerGroup;
import com.whkj.rocketmq.annotation.Handler;
import com.whkj.rocketmq.producer.RocketMQProducer;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RocketMQTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableRocketMQ
@Configuration
public class RocketMQTest {

    @Autowired
    private RocketMQProducer producer;

    @Autowired
    private Set<TestEvents.AddedTestEvent> addedTestEvents;

    @Autowired
    private Set<TestEvents.RemovedTestEvent> removedTestEvents;

    @HandlerGroup
    @Configuration
    static class EventHandlers{
        @Autowired
        private Set<TestEvents.AddedTestEvent> addedTestEvents;

        @Autowired
        private Set<TestEvents.RemovedTestEvent> removedTestEvents;

        @Handler(groupName = "RocketMQTest_handle_AddedTestEvent")
        public void handle(TestEvents.AddedTestEvent event) {
            System.out.println("add->" + event);
            this.addedTestEvents.add(event);
        }

        @Handler(groupName = "RocketMQTest_handle_RemovedTestEvent")
        public void handle(TestEvents.RemovedTestEvent event) {
            System.out.println("remove->" + event);
            this.removedTestEvents.add(event);
        }
    }

    @Before
    public void setup() {
        addedTestEvents.clear();
        removedTestEvents.clear();
    }

    @Test
    public void test() throws InterruptedException {
        Set<TestEvents.AddedTestEvent> addedTestEvents = Sets.newHashSet();
        for (int i = 0; i < 10; i++) {
            addedTestEvents.add(new TestEvents.AddedTestEvent(RandomUtils.nextLong(), String.valueOf(RandomUtils.nextLong())));
        }

        Set<TestEvents.RemovedTestEvent> removedTestEvents = Sets.newHashSet();

        for (int i = 0; i < 10; i++) {
            removedTestEvents.add(new TestEvents.RemovedTestEvent(RandomUtils.nextLong()));
        }

        addedTestEvents.forEach(addedTestEvent -> {
            System.out.println(addedTestEvent.getName() + "-->" + producer.sendMessage(addedTestEvent));
        });

        removedTestEvents.forEach(removedTestEvent -> {
            System.out.println(removedTestEvent.getClass().getName() + "-->" + producer.sendMessage(removedTestEvent));
        });

        TimeUnit.SECONDS.sleep(10);

        synchronized (this) {
            Assert.assertEquals(addedTestEvents, this.addedTestEvents);
            Assert.assertEquals(removedTestEvents, this.removedTestEvents);
        }
    }

    @Configuration
    static class Config {
        @Bean
        public Set<TestEvents.AddedTestEvent> addedTestEvents() {
            return Sets.newConcurrentHashSet();
        }

        @Bean
        public Set<TestEvents.RemovedTestEvent> removedTestEvents() {
            return Sets.newConcurrentHashSet();
        }

    }
}
