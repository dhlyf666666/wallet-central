package com.dhlyf.walletbtc.util.btc;


public interface BitcoinPaymentListener {
    void block(String var1);

    void transaction(Bitcoin.Transaction var1);
}
