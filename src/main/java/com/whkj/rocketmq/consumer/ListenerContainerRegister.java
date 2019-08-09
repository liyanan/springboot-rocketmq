package com.whkj.rocketmq.consumer;

import com.google.common.collect.Maps;
import com.whkj.rocketmq.annotation.HandlerGroup;
import com.whkj.rocketmq.annotation.Handler;
import com.whkj.rocketmq.base.MessageConverter;
import com.whkj.rocketmq.base.MqEvent;
import com.whkj.rocketmq.base.RocketMQProperties;
import com.whkj.rocketmq.util.TopicEventMetaDataUtil;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class ListenerContainerRegister implements ApplicationContextAware, SmartInitializingSingleton {
    private final static Logger LOGGER = LoggerFactory.getLogger(ListenerContainerRegister.class);

    private ConfigurableApplicationContext applicationContext;

    private RocketMQProperties rocketMQProperties;

    private Map<String, Method> consumerGroupMapping = Maps.newHashMap();

    public ListenerContainerRegister(RocketMQProperties rocketMQProperties) {
        this.rocketMQProperties = rocketMQProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(HandlerGroup.class);

        if (Objects.nonNull(beans)) {
            beans.forEach(this::registerContainer);
        }
    }

    private void registerContainer(String beanName, Object bean) {
        Class<?> clazz = AopUtils.getTargetClass(bean);

        List<Method> handlerMethods = getRocketMqHandlerMethods(clazz);
        if (CollectionUtils.isEmpty(handlerMethods)) {
            LOGGER.info("class:{} has no handlers", clazz.getSimpleName());
            return;
        }

        for (Method method : handlerMethods) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 1) {
                LOGGER.warn("method parameter length is not correct");
                throw new IllegalArgumentException("parameter length is not correct");
            }
            //判断参数是否是MqEvent的子类
            if (!MqEvent.class.isAssignableFrom(types[0])) {
                throw new IllegalArgumentException("parameter type is not correct");
            }

            registerContainerByHandler(clazz, method, bean);
        }

    }

    private void registerContainerByHandler(Class clazz, Method method, Object bean) {

        final Handler mqHandler = method.getAnnotation(Handler.class);
        final Class<?> mqEventClazz = method.getParameterTypes()[0];
        String topic = TopicEventMetaDataUtil.getTopic(mqEventClazz);
        String tag = TopicEventMetaDataUtil.getTag(mqEventClazz);

        String containerBeanName = String.format("%s_%s_%s_%s", DefaultRocketMQListenerContainer.class.getSimpleName(),
                clazz.getSimpleName(), method.getName(), mqEventClazz.getSimpleName());
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;

        String groupName = mqHandler.groupName();
        Method registerHandler = this.consumerGroupMapping.get(groupName);
        if (registerHandler != null){
            throw new RuntimeException(String.format("group %s is already used on method %s in class %s", groupName, registerHandler.getName(), clazz.getName()));
        }else {
            this.consumerGroupMapping.put(groupName, method);
        }

        genericApplicationContext.registerBean(containerBeanName, DefaultRocketMQListenerContainer.class,
                () -> new DefaultRocketMQListenerContainer()
                        .nameServer(rocketMQProperties.getNameServer())
                        .consumerGroup(mqHandler.groupName())
                        .topic(topic)
                        .consumeModel(mqHandler.consumeMode())
                        .messageModel(mqHandler.messageModel())
                        .tag(tag)
                        .messageCallback(new MessageCallbackAdapter(bean, method, mqEventClazz)));

        DefaultRocketMQListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                DefaultRocketMQListenerContainer.class);
        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                LOGGER.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Register the listener to container, listenerBeanName:{}, containerBeanName:{}",
                clazz, containerBeanName);

    }


    private class MessageCallbackAdapter implements DefaultRocketMQListenerContainer.MessageCallback{
        private final Object target;
        private final Method method;
        private final Class argType;

        private MessageCallbackAdapter(Object target, Method method, Class argType) {
            this.target = target;
            this.method = method;
            this.argType = argType;
            this.method.setAccessible(true);
        }

        @Override
        public void onMessage(MessageExt messageExt) {
            MqEvent mqEvent = convert(messageExt);
            if (argType.isAssignableFrom(mqEvent.getClass())){
                try {
                    this.method.invoke(this.target, mqEvent);
                } catch (Exception e) {
                    LOGGER.error("failed to call method {} in {} use {}.", method, target.getClass(), mqEvent);
                    throw new RuntimeException(e);
                }
            }else {
                LOGGER.warn("method {} in {} accept {} but arg is {}",method, target.getClass(), this.argType, mqEvent.getClass());
            }

        }
    }

    private MqEvent convert(MessageExt messageExt) {
        return MessageConverter.convertToMqEvent(messageExt);
    }

    private List<Method> getRocketMqHandlerMethods(Class clazz) {
        Method[] methods = clazz.getMethods();
        return Arrays.stream(methods).filter(method -> method.getAnnotation(Handler.class) != null)
                .collect(Collectors.toList());
    }


}
