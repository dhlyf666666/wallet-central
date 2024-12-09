package com.dhlyf.walletsupport.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

@Configuration
@Slf4j
public class RedisConfig {

    @Autowired
    private Environment env;


    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.cluster.nodes")
    @ConditionalOnMissingBean(LettuceConnectionFactory.class)
    public LettuceConnectionFactory redisConnectionFactoryCluster() {
        String clusterNodes = env.getProperty("spring.data.redis.cluster.nodes");
        if (clusterNodes != null && !clusterNodes.isEmpty()) {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(Arrays.asList(clusterNodes.split(",")));
            String password = env.getProperty("spring.data.redis.password");
            if (password != null) {
                clusterConfig.setPassword(RedisPassword.of(password));
            }
            return new LettuceConnectionFactory(clusterConfig);
        }
        return null;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    @ConditionalOnMissingBean(LettuceConnectionFactory.class)
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = env.getProperty("spring.data.redis.host", "localhost");
        int port = env.getProperty("spring.data.redis.port", Integer.class, 6379);
        String password = env.getProperty("spring.data.redis.password");

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            standaloneConfig.setPassword(RedisPassword.of(password));
        }
        log.info("--------------------redisConnectionFactory--------------------");
        log.info("--------------------redisConnectionFactory--------------------");
        log.info("--------------------redisConnectionFactory--------------------");
        log.info("--------------------redisConnectionFactory--------------------");
        log.info("--------------------redisConnectionFactory--------------------");
        log.info("--------------------redisConnectionFactory--------------------");
        return new LettuceConnectionFactory(standaloneConfig);
    }


    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置key序列化方式string，RedisSerializer.string() 等价于 new StringRedisSerializer()
        redisTemplate.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式json，使用GenericJackson2JsonRedisSerializer替换默认序列化，RedisSerializer.json() 等价于 new GenericJackson2JsonRedisSerializer()
        redisTemplate.setValueSerializer(RedisSerializer.json());
        // 设置hash的key的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        // 使配置生效
        redisTemplate.afterPropertiesSet();
        log.info("--------------------RedisTemplate--------------------");
        log.info("--------------------RedisTemplate--------------------");
        log.info("--------------------RedisTemplate--------------------");
        log.info("--------------------RedisTemplate--------------------");
        log.info("--------------------RedisTemplate--------------------");
        log.info("--------------------RedisTemplate--------------------");
        return redisTemplate;
    }

    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        return template;
    }


}
