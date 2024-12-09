package com.dhlyf.walletfil.service;

import com.dhlyf.walletservice.mq.Topic;
import org.springframework.stereotype.Service;

@Service
public class FILTopicImpl implements Topic {


    @Override
    public String getBlockNumber() {
        return "fil_block_number";
    }

    @Override
    public String getBlockTx() {
        return "fil_block_tx";
    }

    @Override
    public String getTx() {
        return "fil_tx";
    }

    @Override
    public String getDeposit() {
        return "fil_deposit";
    }
}
