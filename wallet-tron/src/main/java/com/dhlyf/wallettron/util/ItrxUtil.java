package com.dhlyf.wallettron.util;

import cn.hutool.http.HttpUtil;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.WalletException;
import com.dhlyf.walletmodel.common.WalletResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@Slf4j
public class ItrxUtil {


    private static final String itrxUrl = SpringContextUtil.getProperty("itrx.url");
    private static final String itrxKey = SpringContextUtil.getProperty("itrx.key");
    private static final String itrxSECRET = SpringContextUtil.getProperty("itrx.secret");



    //租用默认能量
    public static void rentEnergy(String address) {
        rentEnergy(address, 32000L, "1H");
    }

    //租用1H默认能量
    public static void rentEnergy1H(String address, Long amount) {
        rentEnergy(address, amount, "1H");
    }

    public static void rentEnergy(String address, Long amount, String period) {
        Map<String, Object> data = new HashMap<>();
        data.put("energy_amount", amount);
        data.put("period", period);
        data.put("receive_address", address);

        // Sorting the keys
        TreeMap<String, Object> sortedData = new TreeMap<>(data);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json_data = gson.toJson(sortedData);

        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        String message = timestamp + "&" + json_data;
        String signature = encodeHmacSHA256(message, itrxSECRET);


        //hutool 代理访问/api/v1/frontend/rent-energy export proxy="http://172.20.6.59:3128"

//        log.info("rentEnergy request: {}", json_data);
//        log.info("rentEnergy timestamp: {}", timestamp);
//        log.info("rentEnergy signature: {}", signature);

        String body = HttpUtil.createPost(itrxUrl + "/api/v1/frontend/order")
                .setHttpProxy("172.20.6.59", 3128)
                .header("API-KEY", itrxKey)
                .header("TIMESTAMP", timestamp)
                .header("SIGNATURE", signature)
                .body(json_data)
                .execute().body();

        log.info("rentEnergy response: {}", body);
    }



    private static String encodeHmacSHA256(String data, String key)  {
        try{
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) {
                    hash.append('0');
                };
                hash.append(hex);
            }
            return hash.toString();
        }catch (Exception e){
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }
}
