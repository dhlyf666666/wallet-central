package com.dhlyf.walletsupport.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedissonUtil {

    private static RedissonClient redissonClient;


    @Autowired(required = false)
    public void setRedissonClient(RedissonClient redissonClient) {
        RedissonUtil.redissonClient = redissonClient;
    }

    public static RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    public static boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = getLock(lockKey);
        return lock.tryLock(waitTime, leaseTime, unit);
    }

    public static void unlock(String lockKey) {
//        log.info("开始释放锁 lockKey={}", lockKey);
        RLock lock = getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public static <T> T withLock(String lockKey, Callable<T> task, long waitTime, long leaseTime, TimeUnit unit) {
        try {
//            log.info("开始尝试获取锁 lockKey={},waitTime={},leaseTime={},unit={}", lockKey, waitTime, leaseTime, unit);
            if (tryLock(lockKey, waitTime, leaseTime, unit)) {
                try {
                    return task.call();
                } finally {
                    unlock(lockKey);
                }
            }else{
//                log.info("获取锁失败 lockKey={}", lockKey);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

