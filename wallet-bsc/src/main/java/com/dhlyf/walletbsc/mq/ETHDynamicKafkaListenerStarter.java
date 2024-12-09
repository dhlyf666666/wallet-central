package com.dhlyf.walletbsc.mq;

import com.dhlyf.walletservice.mq.Topic;
import com.dhlyf.walletservice.service.ChainTopicConsumerService;
import com.dhlyf.walletservice.service.DepositTopicConsumerService;
import com.dhlyf.walletsupport.mq.DynamicKafkaListenerService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ETHDynamicKafkaListenerStarter {

    @Autowired
    private DynamicKafkaListenerService listenerService;
    @Autowired
    private Topic topic;
    @Autowired
    private ChainTopicConsumerService chainTopicConsumerService;
    @Autowired
    private DepositTopicConsumerService depositTopicConsumerService;

    @PostConstruct
    public void init() {
        Map<String, Pair<String, MessageListener<String, String>>> topicGroups = new HashMap<>();
//        topicGroups.put(topic.getBlockNumber(), Pair.of("trx-group", new MessageListener<String, String>() {
//            @Override
//            public void onMessage(ConsumerRecord<String, String> data) {
//                chainTopicConsumerService.onMessage(data);
//            }
//        }));
        topicGroups.put(topic.getBlockNumber(), Pair.of("bsc-group", chainTopicConsumerService::Topic_BLOCK_NUMBER));
        topicGroups.put(topic.getDeposit(), Pair.of("bsc-group", depositTopicConsumerService::Topic_DEPOSIT));
        topicGroups.put(topic.getTx(), Pair.of("bsc-group", chainTopicConsumerService::Topic_TX));

        listenerService.startListeners(topicGroups);
    }

}
