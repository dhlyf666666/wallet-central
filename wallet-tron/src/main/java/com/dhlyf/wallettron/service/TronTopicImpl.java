package com.dhlyf.wallettron.service;

import com.dhlyf.walletservice.mq.Topic;
import org.springframework.stereotype.Service;

@Service
public class TronTopicImpl implements Topic {



    @Override
    public String getBlockNumber() {
        return "block_number";
    }

    @Override
    public String getBlockTx() {
        return "block_tx";
    }

    @Override
    public String getTx() {
        return "tx";
    }

    @Override
    public String getDeposit() {
        return "deposit";
    }
}
