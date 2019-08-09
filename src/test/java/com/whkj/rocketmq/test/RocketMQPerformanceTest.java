package com.whkj.rocketmq.test;

import com.google.common.collect.Sets;
import com.whkj.rocketmq.TestEvents;
import com.whkj.rocketmq.annotation.HandlerGroup;
import com.whkj.rocketmq.annotation.Handler;
import com.whkj.rocketmq.base.RocketMQProperties;
import com.whkj.rocketmq.consumer.ListenerContainerRegister;
import com.whkj.rocketmq.producer.DefaultRocketMQProducer;
import com.whkj.rocketmq.producer.RocketMQProducer;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RocketMQPerformanceTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@HandlerGroup
public class RocketMQPerformanceTest {

    @Autowired
    private RocketMQProducer producer;

    @Test
    @Ignore
    public void test() throws InterruptedException {
        int size = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        for (int i=0;i<size;i++){
            executorService.submit((Runnable) () -> {
                int count = 100000;
                while (count-- > 0) {
                    Set<TestEvents.AddedTestEvent> addedTestEvents = Sets.newHashSet();
                    for (int i1 = 0; i1 < 100; i1++) {
                        addedTestEvents.add(new TestEvents.AddedTestEvent(RandomUtils.nextLong(), String.valueOf(RandomUtils.nextLong())));
                    }

                    Set<TestEvents.RemovedTestEvent> removedTestEvents = Sets.newHashSet();

                    for (int i1 = 0; i1 < 100; i1++) {
                        removedTestEvents.add(new TestEvents.RemovedTestEvent(RandomUtils.nextLong()));
                    }

                    addedTestEvents.forEach(addedTestEvent -> {
                        producer.sendMessage(addedTestEvent);
                    });

                    removedTestEvents.forEach(removedTestEvent -> {
                        producer.sendMessage(removedTestEvent);
                    });
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

    }


    @Handler(groupName = "RocketMQTest_handle_AddedTestEvent")
    public void handle(TestEvents.AddedTestEvent event){
//        System.out.println("add->" + event);
    }

    @Handler(groupName = "RocketMQTest_handle_RemovedTestEvent")
    public void handle(TestEvents.RemovedTestEvent event){
//        System.out.println("remove->" + event);
    }

    @Configuration
    static class Config{

        @Bean
        public ListenerContainerRegister configuration(){
            return new ListenerContainerRegister(properties());
        }

        @Bean
        public RocketMQProperties properties(){
            RocketMQProperties properties = new RocketMQProperties();
            properties.setNameServer("127.0.0.1:9876");
            return properties;
        }

        @Bean
        public DefaultRocketMQProducer producer(){
            return new DefaultRocketMQProducer("TEST_CASE", properties());
        }
    }
}
