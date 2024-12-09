package com.dhlyf.walletfil.service;

import com.dhlyf.walletmodel.common.RedisKey;
import org.springframework.stereotype.Service;

@Service
public class FILRedisKeyImpl implements RedisKey {

    @Override
    public String getRedisContractPrefix() {
        return "fil_contract_";
    }

    @Override
    public String getRedisAddressPrefix() {
        return "fil_address_";
    }

    @Override
    public String getRedisMerchantIdPrefix() {
        return "fil_merchant_id_";
    }

    @Override
    public String getRedisLastetNumber() {
        return "fil_lastetNumber_redis";
    }

    @Override
    public String getRedisLastetId() {
        return "fil_address_db_lastetId";
    }

    @Override
    public String getRedisHashPrefix() {
        return "fil_hash_";
    }
}
