package com.dhlyf.walletbtc.util.btc;

public class BitcoinRPCException extends BitcoinException {
    public BitcoinRPCException() {
    }

    public BitcoinRPCException(String msg) {
        super(msg);
    }

    public BitcoinRPCException(Throwable cause) {
        super(cause);
    }

    public BitcoinRPCException(String message, Throwable cause) {
        super(message, cause);
    }
}
