package com.dhlyf.walletbsc.service;

import com.dhlyf.walletmodel.common.RedisKey;
import org.springframework.stereotype.Service;

@Service
public class ETHRedisKeyImpl implements RedisKey {

    @Override
    public String getRedisContractPrefix() {
        return "eth_contract_";
    }

    @Override
    public String getRedisAddressPrefix() {
        return "eth_address_";
    }

    @Override
    public String getRedisMerchantIdPrefix() {
        return "eth_merchant_id_";
    }

    @Override
    public String getRedisLastetNumber() {
        return "eth_lastetNumber_redis";
    }

    @Override
    public String getRedisLastetId() {
        return "eth_address_db_lastetId";
    }

    @Override
    public String getRedisHashPrefix() {
        return "eth_hash_";

    }
}
