package com.dhlyf.walletservice.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dhlyf.walletdao.mybatis.orm.Address;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.RedisKey;
import com.dhlyf.walletservice.mq.KafkaProducer;
import com.dhlyf.walletservice.mq.Topic;
import com.dhlyf.walletservice.service.AddressService;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.ContractService;
import com.dhlyf.walletservice.service.TronJobService;
import com.dhlyf.walletsupport.utils.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
@Slf4j
//@DependsOn({"redisUtil"})
public class TronJobServiceImpl implements TronJobService {


    @Autowired
    private KafkaProducer kafkaProducer;
    @Autowired
    private ContractService contractService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private RedisKey redisKey;
    @Autowired
    private Topic topic;
    @Autowired
    private ChainService chainService;

//    @PostConstruct
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        initBlock();
        initContract();
        initAddress();
    }

    //初始化块搞
    public void initBlock() {
        //redis里面取最新块高度
        Object lastetNumber = RedisUtil.get(redisKey.getRedisLastetNumber());
        //如果值为空，从区块链上获取最新块高度
        if (lastetNumber == null) {
            lastetNumber = chainService.getNowBlockNumber();
            RedisUtil.set(redisKey.getRedisLastetNumber(), lastetNumber);
        }
    }

    //初始化地址
    public void initAddress() {
        //redis里面取最新地址id
        Object lastetIdRedis = RedisUtil.get(redisKey.getRedisLastetId());
        long lastetId = 1;
        //如果值为空，从数据库里面获取最新地址id
        if (lastetIdRedis == null) {
            RedisUtil.set(redisKey.getRedisLastetId(), lastetId);
        }else{
            lastetId = Long.valueOf(lastetIdRedis.toString());
        }

        List<Address> list = addressService.queryByIdGreaterThanOrEqual(lastetId);

        if (!CollectionUtils.isEmpty(list)) {
            for (Address address : list) {
                RedisUtil.set(redisKey.getRedisAddressPrefix() + address.getAddress(), JSONObject.toJSONString(address));
                RedisUtil.set(redisKey.getRedisLastetId(), address.getId());
            }
        }
    }

    //初始化合约
    public void initContract() {
        LambdaQueryWrapper<Coin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coin::getChainType, chainService.getChain());
        List<Coin> list = contractService.list(queryWrapper);
        if(!CollectionUtils.isEmpty(list)) {
            for (Coin contract : list) {
                log.info("加载币种 {}  合约 {} ", contract.getCoinName(), contract.getAddress());
                RedisUtil.set(redisKey.getRedisContractPrefix() + contract.getAddress().toUpperCase(), JSONObject.toJSONString(contract));
            }
        }
    }


    @Override
    public void scanBlock() {
        long lastetNumber = chainService.getNowBlockNumber();
        //获取redis里面的最新块高度
        int redisLastetNumber = (int)RedisUtil.get(redisKey.getRedisLastetNumber());
        //默认值
        if (redisLastetNumber == 0) {
            redisLastetNumber = 0;
        }
        //如果区块链上的最新块高度大于redis里面的最新块高度
        log.info("区块链上的最新块高度：{}，redis里面的最新块高度：{}", lastetNumber, redisLastetNumber);
        while (lastetNumber-19 > redisLastetNumber) {
            //从redis里面取出最新块高度
            long startNumber = redisLastetNumber + 1;
            //块信息发送到kafka
            log.info("发送块高：{}", startNumber);
            kafkaProducer.sendMessage(topic.getBlockNumber(), startNumber+"");
            //更新redis里面的最新块高度
            RedisUtil.set(redisKey.getRedisLastetNumber(), startNumber);
            redisLastetNumber = (int)RedisUtil.get(redisKey.getRedisLastetNumber());
        }
    }


}
