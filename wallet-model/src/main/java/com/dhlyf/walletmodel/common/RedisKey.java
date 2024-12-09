package com.dhlyf.walletmodel.common;

public interface RedisKey {


    String getRedisContractPrefix();
    String getRedisAddressPrefix();
    String getRedisMerchantIdPrefix();
    String getRedisLastetNumber();
    String getRedisLastetId();

    String getRedisHashPrefix();


}
