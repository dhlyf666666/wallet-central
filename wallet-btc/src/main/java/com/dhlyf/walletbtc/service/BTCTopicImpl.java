package com.dhlyf.walletbtc.service;

import com.dhlyf.walletservice.mq.Topic;
import org.springframework.stereotype.Service;

@Service
public class BTCTopicImpl implements Topic {


    @Override
    public String getBlockNumber() {
        return "btc_block_number";
    }

    @Override
    public String getBlockTx() {
        return "btc_block_tx";
    }

    @Override
    public String getTx() {
        return "btc_tx";
    }

    @Override
    public String getDeposit() {
        return "btc_deposit";
    }
}
