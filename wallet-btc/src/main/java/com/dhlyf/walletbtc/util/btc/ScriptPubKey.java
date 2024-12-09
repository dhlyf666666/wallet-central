package com.dhlyf.walletbtc.util.btc;

public class ScriptPubKey {
    private String address;

    public ScriptPubKey(String address) {
        this.address = address;
    }

    // Getter and Setter
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
