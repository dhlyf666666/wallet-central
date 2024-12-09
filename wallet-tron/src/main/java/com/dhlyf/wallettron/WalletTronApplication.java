package com.dhlyf.wallettron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.io.support.SpringFactoriesLoader;

@SpringBootApplication
public class WalletTronApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletTronApplication.class, args);
    }

}
