package com.dhlyf.walletbtc.service;

import com.dhlyf.walletmodel.common.RedisKey;
import org.springframework.stereotype.Service;

@Service
public class BTCRedisKeyImpl implements RedisKey {

    @Override
    public String getRedisContractPrefix() {
        return "btc_contract_";
    }

    @Override
    public String getRedisAddressPrefix() {
        return "btc_address_";
    }

    @Override
    public String getRedisMerchantIdPrefix() {
        return "btc_merchant_id_";
    }

    @Override
    public String getRedisLastetNumber() {
        return "btc_lastetNumber_redis";
    }

    @Override
    public String getRedisLastetId() {
        return "btc_address_db_lastetId";
    }

    @Override
    public String getRedisHashPrefix() {
        return "btc_hash_";
    }
}
