package com.dhlyf.walletservice.mq;

public interface Topic {

    String getBlockNumber();
    String getBlockTx();
    String getTx();
    String getDeposit();
    //块编号队列



}
