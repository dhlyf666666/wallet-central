package com.dhlyf.walletaleo.service;

import com.dhlyf.walletmodel.common.RedisKey;
import org.springframework.stereotype.Service;

@Service
public class AleoRedisKeyImpl implements RedisKey {

    @Override
    public String getRedisContractPrefix() {
        return "aleo_contract_";
    }

    @Override
    public String getRedisAddressPrefix() {
        return "aleo_address_";
    }

    @Override
    public String getRedisMerchantIdPrefix() {
        return "aleo_merchant_id_";
    }

    @Override
    public String getRedisLastetNumber() {
        return "aleo_lastetNumber_redis";
    }

    @Override
    public String getRedisLastetId() {
        return "aleo_address_db_lastetId";
    }

    @Override
    public String getRedisHashPrefix() {
        return "aleo_hash_";

    }
}
