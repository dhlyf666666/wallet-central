package com.dhlyf.walletsupport.base;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class RedissonConfig {

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.cluster.nodes")
    public RedissonClient redissonConnectionFactoryCluster() {
        String clusterNodes = env.getProperty("spring.data.redis.cluster.nodes");
        String password = env.getProperty("spring.data.redis.password");
        if (clusterNodes != null && !clusterNodes.isEmpty()) {
            // 分割节点字符串，并为每个节点添加 "redis://" 前缀
            String[] nodes = clusterNodes.split(",");
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = "redis://" + nodes[i].trim();
            }

            Config config = new Config();
            config.useClusterServers()
                    .setPassword(password)
                    .addNodeAddress(nodes); // 添加处理过的节点地址

            return Redisson.create(config);
        }
        return null;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public RedissonClient redissonConnectionFactory() {
        String host = env.getProperty("spring.data.redis.host", "localhost");
        int port = env.getProperty("spring.data.redis.port", Integer.class, 6379);
        String password = env.getProperty("spring.data.redis.password");

        Config config = new Config();
        String redisUrl = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(redisUrl)
                .setPassword(password);

        return Redisson.create(config);
    }
}
