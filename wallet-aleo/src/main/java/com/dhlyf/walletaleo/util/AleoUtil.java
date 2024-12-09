package com.dhlyf.walletaleo.util;


import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.dhlyf.walletaleo.util.aleo.BlockTransactions;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AleoUtil {


    private static String rpcHttpUrl = SpringContextUtil.getProperty("aleo.rpcHttpUrl");
    private static String rpcHttpUrlBroadcast = SpringContextUtil.getProperty("aleo.rpcHttpUrl.broadcast");


    private static final String coolWalletUrl = SpringContextUtil.getProperty("coolwallet.url");

    private static final String hotWalletAddress = SpringContextUtil.getProperty("hot.wallet.address");

    private static final String hotWalletPrivateKey = SpringContextUtil.getProperty("hot.wallet.privatekey");



    public static String getHotWalletAddress() {
        return hotWalletAddress;
    }

    public static Long getNowBlockNumber() {
        String url = rpcHttpUrl + "/mainnet/latest/height";
        log.info("url={}", url);
        HttpResponse response = HttpUtil.createGet(url)
//                .setHttpProxy("172.20.6.59", 3128)
                .execute();
        String height = response.body();
        return Long.valueOf(height);
    }

    public static List<BlockTransactions> getTransactionsList(long blockNumber) {
        String url = rpcHttpUrl + "/mainnet/block/"+ blockNumber + "/transactions";
        log.info("url={}", url);
        HttpResponse response = HttpUtil.createGet(url)
//                .setHttpProxy("172.20.6.59", 3128)
                .execute();
        String transactionStr = response.body();
        if(ObjectUtils.isEmpty(transactionStr)){
            return new ArrayList<>();
        }
        try{
            List<BlockTransactions> transactionsList =
                    JSONObject.parseObject(transactionStr, new TypeReference<List<BlockTransactions>>() {});
            return transactionsList;
        }catch (Exception e){
            log.info("aleo.transactionsList={}", transactionStr);
            e.printStackTrace();
        }
        return null;
    }

    public static String transferRaw(String address, BigDecimal amount) {
        JSONObject requestData = new JSONObject();
        requestData.put("from_address",hotWalletAddress);
        requestData.put("private_key",hotWalletPrivateKey);
        requestData.put("amount",amount.stripTrailingZeros().toPlainString());
        requestData.put("to_address",address);
        String rawSign = HttpUtil.createPost(coolWalletUrl+"/sign")
                .header("Content-Type", "application/json")
                .body(requestData.toJSONString())
                .execute().body();


        log.info("transferRaw request {}",requestData.toJSONString());
        log.info("transferRaw rawSign {}",rawSign);

        JSONObject rawData = JSONObject.parseObject(rawSign);
        String hash = rawData.getString("id");

        String broadcastResult = HttpUtil.createPost(rpcHttpUrlBroadcast+"/mainnet/transaction/broadcast")
//                .setHttpProxy("172.20.6.59", 3128)
                .header("Content-Type", "application/json")
                .body(rawSign)
                .execute().body();

        log.info("transferRaw broadcastResult {}",broadcastResult);
        return broadcastResult.replaceAll("\"", "");
    }


    public static BigDecimal getBalance(String address) {
        String url = rpcHttpUrl + "/mainnet/program/credits.aleo/mapping/account/" + address;
        log.info("Fetching balance from URL: {}", url);

        try {
            HttpResponse response = HttpUtil.createGet(url)
                    .execute();
            String balanceStr = response.body();

            if (ObjectUtils.isEmpty(balanceStr)) {
                log.warn("No balance found for address: {}", address);
                return BigDecimal.ZERO;
            }

            // Remove any extra characters from the response, e.g., "u64" if it exists
            balanceStr = balanceStr.replaceAll("[^\\d]", ""); // Extract only numeric part

            // Convert the balance to BigDecimal (Aleo might return balance as u64 integer format)
            BigDecimal balance = new BigDecimal(balanceStr).divide(new BigDecimal("1000000"));
            log.info("Balance for address {} is: {}", address, balance);
            return balance;
        } catch (Exception e) {
            log.error("Error fetching balance for address: {}", address, e);
            return BigDecimal.ZERO; // Return zero if any error occurs
        }
    }
}
