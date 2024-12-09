package com.dhlyf.wallettron.service;

import com.dhlyf.walletmodel.common.RedisKey;
import org.springframework.stereotype.Service;

@Service
public class TronRedisKeyImpl implements RedisKey {

    @Override
    public String getRedisContractPrefix() {
        return "trx_contract_";
    }

    @Override
    public String getRedisAddressPrefix() {
        return "trx_address_";
    }

    @Override
    public String getRedisMerchantIdPrefix() {
        return "trx_merchant_id_";
    }

    @Override
    public String getRedisLastetNumber() {
        return "trx_lastetNumber_redis";
    }

    @Override
    public String getRedisLastetId() {
        return "trx_address_db_lastetId";
    }

    @Override
    public String getRedisHashPrefix() {
        return "trx_hash_";

    }
}
