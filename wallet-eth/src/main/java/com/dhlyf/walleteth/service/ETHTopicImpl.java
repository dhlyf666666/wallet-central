package com.dhlyf.walleteth.service;

import com.dhlyf.walletservice.mq.Topic;
import org.springframework.stereotype.Service;

@Service
public class ETHTopicImpl implements Topic {



    @Override
    public String getBlockNumber() {
        return "eth_block_number";
    }

    @Override
    public String getBlockTx() {
        return "eth_block_tx";
    }

    @Override
    public String getTx() {
        return "eth_tx";
    }

    @Override
    public String getDeposit() {
        return "eth_deposit";
    }
}
