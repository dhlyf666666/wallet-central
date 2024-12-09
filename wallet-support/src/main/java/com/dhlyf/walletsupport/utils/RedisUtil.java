package com.dhlyf.walletsupport.utils;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
//@DependsOn({"redisTemplate", "stringRedisTemplate"})
public class RedisUtil {


    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 静态字段
    private static RedisTemplate<String, Object> redisTemplate;
    private static StringRedisTemplate stringRedisTemplate;

    public static void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public static Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public static void delete(String key) {
        redisTemplate.delete(key);
    }

    public static void setString(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public static void setString(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public static String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }


    // Hash operations
    public static void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public static Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public static Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public static void hDelete(String key, String... fields) {
        redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    public static boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public static Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    public static long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public static Boolean expire(String key, long time) {
        Boolean result = false;
        try {
            if (StringUtils.isNotBlank(key) && time > 0) {
                result = redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return result;
    }

    public static boolean exists(String s) {
        return redisTemplate.hasKey(s);
    }

    public static void set(String s, String hash, int i) {
        redisTemplate.opsForHash().put(s, s, hash);
        redisTemplate.expire(s, i, TimeUnit.SECONDS);
    }
}

