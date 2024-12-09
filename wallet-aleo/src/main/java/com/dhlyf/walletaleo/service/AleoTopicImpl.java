package com.dhlyf.walletaleo.service;

import com.dhlyf.walletservice.mq.Topic;
import org.springframework.stereotype.Service;

@Service
public class AleoTopicImpl implements Topic {



    @Override
    public String getBlockNumber() {
        return "aleo_block_number";
    }

    @Override
    public String getBlockTx() {
        return "aleo_block_tx";
    }

    @Override
    public String getTx() {
        return "aleo_tx";
    }

    @Override
    public String getDeposit() {
        return "aleo_deposit";
    }
}
