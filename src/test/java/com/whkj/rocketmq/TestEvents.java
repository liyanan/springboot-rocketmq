package com.whkj.rocketmq;

import com.whkj.rocketmq.annotation.Topic;
import com.whkj.rocketmq.base.MqEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Topic("outter_topic")
public interface TestEvents {

    @Topic("test_agg_topic")
    @Data
    @EqualsAndHashCode
    abstract class AbstractTestEvent implements MqEvent {
        private final Long id;

        protected AbstractTestEvent(Long id) {
            this.id = id;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    class AddedTestEvent extends AbstractTestEvent {
        private final String name;
        public AddedTestEvent(Long id, String name) {
            super(id);
            this.name = name;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    class RemovedTestEvent extends AbstractTestEvent {
        public RemovedTestEvent(Long id) {
            super(id);
        }
    }

    @Topic("test_agg_topic_2")
    class Removed2TestEvent extends AbstractTestEvent {
        public Removed2TestEvent(Long id) {
            super(id);
        }
    }

    class OutterEvent{

    }
}
