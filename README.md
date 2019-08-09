# Spring Boot集成RocketMQ

##### Function
```$x
将RocketMQ集成到Spring Boot,通过简单的几个注解即可实现RocketMQ消息的发送和消费
```
##### Quick Start
**Step 1:**
```
通过注解@EnableRocketMQ即可开启RocketMQ生产者和消费者功能，可只开启生产者或只开启消费者
开启生产者: producer=true 
开启消费者: consumer=true 

```
**Step 2:**
```
消费者类通过 @HandlerGroup 标注
通过@Handler(groupName = "XXX") 标注于方法上表示需要消费的消息,消息需实现MqEvent接口
e.g.

    @HandlerGroup
    public class RocketMqConsumerTest<Event extends MqEvent> {
    
       @Handler(groupName = "RocketMqConsumerTest:handle")
       public void handle(Event mqEvent){
    
       }
    }

生产者：
e.g.
1、类或方法上通过@Topic 注解标注要发送到的topic
2、消息需要实现MqEvent 或者实现AbstractEvent
e.g.

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
}
```



 



  

  

