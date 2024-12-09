package com.dhlyf.walletsupport.base;

import com.dhlyf.walletsupport.mq.DynamicKafkaListenerService;
import com.dhlyf.walletsupport.utils.RedisUtil;
import com.dhlyf.walletsupport.utils.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Import({
        RedisConfig.class,
        RedissonConfig.class,
        RedissonUtil.class,
        RedisUtil.class,
        DynamicKafkaListenerService.class,
//        RabbitConfig.class
})
@Configuration
@Slf4j
public class AutoConfiguration {



}
