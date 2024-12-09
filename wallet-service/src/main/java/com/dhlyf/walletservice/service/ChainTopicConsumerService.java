package com.dhlyf.walletservice.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

public interface ChainTopicConsumerService {

    void Topic_BLOCK_NUMBER(ConsumerRecord<String, String> consumerRecord);

    void Topic_TX(ConsumerRecord<String, String> consumerRecord);
}
