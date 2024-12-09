package com.dhlyf.walletservice.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface DepositTopicConsumerService {

    void Topic_DEPOSIT(ConsumerRecord<String, String> consumerRecord);
}
