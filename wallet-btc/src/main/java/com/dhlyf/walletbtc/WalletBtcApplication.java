package com.dhlyf.walletbtc;

import com.dhlyf.walletbtc.util.btc.BitcoinRPCClient;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.MalformedURLException;

@SpringBootApplication
public class WalletBtcApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletBtcApplication.class, args);
    }

    @Value("${btc.rcp.address}")
    private String rpcAddress;
    @Value("${btc.wallet.path}")
    private String walletPath;
    @Value("coolwallet.url")
    private String coolWalletUrl;

    @Bean
    public BitcoinRPCClient initClient(){
        try {
            BitcoinRPCClient client =  new BitcoinRPCClient(rpcAddress,walletPath,coolWalletUrl);
//            int blockCount = client.getBlockCount();
            return client;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
