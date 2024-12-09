package com.dhlyf.walletsupport.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DynamicKafkaListenerService {

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, String> factory;

    // 为一个主题和组ID启动一个监听器，并接受一个消息处理器
    public void startListener(String topic, String groupId, MessageListener<String, String> messageListener) {
        // 直接使用主题名称创建消息监听器容器
        ConcurrentMessageListenerContainer<String, String> container = factory.createContainer(topic);

        // 设置容器的其他属性
        container.getContainerProperties().setGroupId(groupId);
        container.getContainerProperties().setMessageListener(messageListener);
        container.setBeanName("ContainerFor" + topic + groupId);

        // 启动容器
        container.start();
    }

    // 启动多个监听器，每个都有其专属的消息处理逻辑
    public void startListeners(Map<String, Pair<String, MessageListener<String, String>>> topicGroups) {
        topicGroups.forEach((topic, groupAndListener) ->
                startListener(topic, groupAndListener.getFirst(), groupAndListener.getSecond()));
    }
}
