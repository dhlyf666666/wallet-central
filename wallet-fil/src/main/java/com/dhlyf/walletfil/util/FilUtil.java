package com.dhlyf.walletfil.util;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletfil.model.fil.*;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CommonResult;
import com.dhlyf.walletmodel.common.WalletException;
import com.dhlyf.walletmodel.common.WalletResult;
import com.dhlyf.walletmodel.web.req.RequestData;
import com.dhlyf.walletmodel.web.req.cool.CoolWalletFormatParam;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;



@Slf4j
public class FilUtil {

    private static final String rpcAddress = SpringContextUtil.getProperty("fil.rpcAddress");;

    private static final String hotWalletAddress = SpringContextUtil.getProperty("hot.wallet.address");

    private static final String hotWalletPrivateKey = SpringContextUtil.getProperty("hot.wallet.privatekey");
    private static final String coolWalletUrl = SpringContextUtil.getProperty("coolwallet.url");

    public static Long getNowBlockNumber() {

        List<Object> info = new ArrayList<>();

        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method(FilecoinCnt.ChainHead)
                .params(info).build();

        TransactionsFil response =  HttpRequest.post(URI.create(rpcAddress))
                .addHeader("Content-Type", "application/json")
//                .authenticator(new BaseAuthenticator(PROJECT_ID, SECRET))
                .bodyJson(par)
                .execute()
                .asValue(TransactionsFil.class);

        return response.getResult().getHeight();
    }


    public static List<String> getBlockByNumber(long number) {

        List<Object> info = new ArrayList<>();
        info.add(number);
        info.add(new ArrayList<>());
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method(FilecoinCnt.ChainGetTipSetByHeight)
                .params(info).build();

        log.info(rpcAddress);
        TransactionsFil response =  HttpRequest.post(URI.create(rpcAddress))
                .addHeader("Content-Type", "application/json")
//                .authenticator(new BaseAuthenticator(PROJECT_ID, SECRET))
                .bodyJson(par)
                .execute()
                .asValue(TransactionsFil.class);
        List<String> filCids = new ArrayList<>();
        if (number == response.getResult().getHeight()){
            for(Result.CidItem cidItem: response.getResult().getCids()){
                filCids.add(cidItem.getCid());
            }
        }
//        logger.info(filCids.toString());
        return filCids;
    }

    public static List<MessageItem> getChainGetBlockMessages(String cid) {

        JSONObject cidObj = new JSONObject();
        cidObj.put("/", cid);

        List<Object> info = new ArrayList<>();
        info.add(cidObj);

        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method(FilecoinCnt.ChainGetBlockMessages)
                .params(info).build();

        TransactionsFil response =  HttpRequest.post(URI.create(rpcAddress))
                .addHeader("Content-Type", "application/json")
//                .authenticator(new BaseAuthenticator(PROJECT_ID, SECRET))
                .bodyJson(par)
                .execute()
                .asValue(TransactionsFil.class);
        List<Result.SecpkMessagesItem> SecpkMessagesList = new ArrayList<>();
//        log.info("response: {}",response.toString());
        SecpkMessagesList.addAll(response.getResult().getSecpkMessages());

        List<MessageItem> MessagesList = new ArrayList<>();
        for(Result.SecpkMessagesItem SecpkMessages :SecpkMessagesList){
            Result.CidItem cidItem = SecpkMessages.getCID();
            MessageItem messageItem = SecpkMessages.getMessage();
            messageItem.setCID(cidItem);
            MessagesList.add(messageItem);
        }
        return MessagesList;
    }

    public static String getHotWalletAddress() {
        return hotWalletAddress;
    }


    public static String transferFilRaw(String address, BigDecimal amount){
        return transferFilRaw(address, amount, hotWalletAddress, hotWalletPrivateKey);
    }
//    public static String transferFilRaw(String address, BigDecimal amount) {
//
//        BigInteger valueInAttoFIL = filToAttoFIL(amount);
//
//
//        //获取gas
//        GasResult gas = getGas(GetGas.builder().from(hotWalletAddress)
//                .to(address)
//                .value(valueInAttoFIL).build());
//        //获取nonce
//        int nonce = getNonce(hotWalletAddress);
//        //拼装交易参数
//        Transaction transaction = Transaction.builder().from(hotWalletAddress)
//                .to(address)
//                .gasFeeCap(gas.getGasFeeCap())
//                .gasLimit(gas.getGasLimit().longValue() * 2)
//                .gasPremium(gas.getGasPremium())
//                .method(0L)
//                .nonce((long) nonce)
//                .params("")
//                .value(valueInAttoFIL.toString()).build();
//
//        return send(transaction, hotWalletPrivateKey);
//    }

    private static String send(Transaction transaction, String privatekey) {
        if (transaction == null || StrUtil.isBlank(transaction.getFrom())
                || StrUtil.isBlank(transaction.getTo())
                || StrUtil.isBlank(transaction.getGasFeeCap())
                || StrUtil.isBlank(transaction.getGasPremium())
                || StrUtil.isBlank(transaction.getValue())
                || transaction.getGasLimit() == null
                || transaction.getMethod() == null
                || transaction.getNonce() == null
                || StrUtil.isBlank(privatekey)) {
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
        BigInteger account = new BigInteger(transaction.getValue());
        if (account.compareTo(BigInteger.ZERO) < 0) {
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
        byte[] cidHash = null;
        try {
            cidHash = TransactionHandler.transactionSerialize(transaction);
//            String folderPath = HexUtil.encodeHexStr(cidHash);
//            System.out.println("cidHash :" + folderPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }


        FilRaw filRaw = new FilRaw();
        filRaw.setData(Base64.encode(cidHash));
        filRaw.setPrivateKey(privatekey);
        log.info("filRaw:{}", filRaw);
        RequestData requestData = CoolWalletFormatParam.formatParma(filRaw, Chain.FIL);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);
        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/fil")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        log.info("coolwallet param{} commonResult:{}", requestCoolWalletParam, commonResult);
        if(commonResult.getCode() == 0){
            String signReulst = (String) commonResult.getData();

//            System.out.println("sing = "+signReulst);
            List<Object> params = new ArrayList<>();
            cn.hutool.json.JSONObject signatureJson = new cn.hutool.json.JSONObject();
            cn.hutool.json.JSONObject messageJson = new cn.hutool.json.JSONObject(transaction);
            cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject();
            messageJson.putOpt("version", 0);
            signatureJson.putOpt("data", signReulst);
            signatureJson.putOpt("type", 1);
            json.putOpt("message", messageJson);
            json.putOpt("signature", signatureJson);

            params.add(json);
            RpcPar rpcPar = RpcPar.builder().id(1).jsonrpc("2.0").method(FilecoinCnt.BOARD_TRANSACTION)
                    .params(params).build();

            String execute = execute(rpcPar);
            log.info("execute param:{} result:{}", JSONObject.toJSONString(params), execute);
            SendResult build = null;
            try {
                cn.hutool.json.JSONObject executeJson = new cn.hutool.json.JSONObject(execute);
                String result = executeJson.getJSONObject("result").getStr("/");
                build = SendResult.builder().cid(result)
                        .nonce(transaction.getNonce()).build();
            } catch (Exception e) {
                e.printStackTrace();
                throw new WalletException.Builder()
                        .code(WalletResult.UNKNOWN_ERROR.getCode())
                        .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                        .build();
            }
            return build.getCid();

        }else{
            log.error("toAddress:{} amount:{} transferFilRaw error:{}", transaction.getTo(),
                    transaction.getValue(), coolWalletResult);
            throw new RuntimeException(coolWalletResult);
        }

    }

    public static int getNonce(String address) {
        List<Object> params = new ArrayList<>();
        params.add(address);
        RpcPar par = RpcPar.builder().id(1).jsonrpc("2.0").method(FilecoinCnt.GET_NONCE)
                .params(params).build();
        String execute = execute(par);
        Integer num = 0;
        try {
            JSONObject result = JSONObject.parseObject(execute);
//            System.out.println(result);
            num = result.getInteger("result");
        } catch (Exception e) {
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
        return num;
    }


    public static BigInteger filToAttoFIL(BigDecimal filValue) {
        // 定义转换比例：1 FIL = 10^18 attoFIL
        BigInteger conversionFactor = new BigInteger("1000000000000000000");

        // 将FIL单位的金额转换为attoFIL单位
        BigInteger attoFILValue = filValue.multiply(new BigDecimal(conversionFactor)).toBigInteger();

        return attoFILValue;
    }


    public static BigDecimal attoFILToFIL(BigInteger attoFIL) {
        // 定义转换比例：1 FIL = 10^18 attoFIL

        BigDecimal ATTOFIL_PER_FIL = new BigDecimal("1000000000000000000");

        return new BigDecimal(attoFIL).divide(ATTOFIL_PER_FIL);
    }
    public static GasResult getGas(GetGas gas) {

        List<Object> params = new ArrayList<>();
        JSONObject json = new JSONObject();
        json.put("From", gas.getFrom());
        json.put("To", gas.getTo());
        json.put("Value", gas.getValue().toString());
        params.add(json);
        params.add(null);
        params.add(null);
        RpcPar par = RpcPar.builder().id(1).jsonrpc("2.0")
                .params(params)
                .method(FilecoinCnt.GET_GAS).build();
        String execute = execute(par);
        log.info("execute gas params:{} result : {}", JSONObject.toJSONString(gas), execute);
        GasResult gasResult = null;
        try {
            JSONObject result = JSONObject.parseObject(execute);
            JSONObject jsonObject = result.getJSONObject("result");
            gasResult = GasResult.builder().gasFeeCap(jsonObject.getString("GasFeeCap"))
                    .gasLimit(jsonObject.getBigInteger("GasLimit"))
//                    .gasLimit(new BigInteger("10000000000000"))
                    .gasPremium(jsonObject.getString("GasPremium")).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
        return gasResult;
    }

    /**
     * 查询地址余额
     *
     * @param address 钱包地址
    //     * @param timeout 时时间 单位：ms
     * @return BalanceResult
     */
    public static BalanceResult getBalance(String address) {
        BalanceResult result = new BalanceResult();
        if (StrUtil.isBlank(address)) {
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
        List<Object> params = new ArrayList<>();
        params.add(address);
        RpcPar par = RpcPar.builder().id(1)
                .jsonrpc("2.0")
                .method(FilecoinCnt.GET_BALANCE)
                .params(params).build();

        BalanceResult execute = HttpRequest.post(URI.create(rpcAddress))
                .addHeader("Content-Type", "application/json")
//                .authenticator(new BaseAuthenticator(PROJECT_ID, SECRET))
                .bodyJson(par)
                .execute()
                .asValue(BalanceResult.class);
        result.setBalance(filUtil(execute.getResult()));
        return result;
    }

    private static String execute(RpcPar par) {
        try {
            // 使用 Hutool 构建和发送 HTTP POST 请求
            HttpResponse response = cn.hutool.http.HttpRequest.post(rpcAddress)
                    .header("Content-Type", "application/json") // 设置请求头
                    .body(JSONUtil.toJsonStr(par)) // 将参数对象转换为 JSON 字符串
                    .execute(); // 执行请求

            if (response.isOk()) {
                // 获取响应的正文内容
                String responseBody = response.body();
                return responseBody;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
//    private static String execute(RpcPar par) {
//
//        HttpClient httpClient = HttpClients.createDefault();
//
//        // Create HTTP POST request
//        HttpPost httpPost = new HttpPost(rpcAddress);
//        httpPost.setHeader("Content-Type", "application/json");
//
//        try {
//            httpPost.setEntity(new StringEntity(JSONObject.toJSONString(par)));
//            // Execute the request
//            org.apache.http.HttpResponse response = httpClient.execute(httpPost);
//            // Handle the response
//            String responseBody = readResponse(response.getEntity());
//            JSONObject jsonObject = JSONObject.parseObject(responseBody);
//            httpClient = null;
//            return responseBody;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    private static String readResponse(HttpEntity entity) throws Exception {
//        java.util.Scanner scanner = new java.util.Scanner(entity.getContent()).useDelimiter("\\A");
//        return scanner.hasNext() ? scanner.next() : "";
//    }

    /**
     * 18 位 fil 转 8 位BigDecimal
     *
     * @param fil
     * @return
     */
    public static BigDecimal filUtil(String fil) {
        if (fil.equals(CodeConstants.FIL)) {
            return new BigDecimal(CodeConstants.FIL_DECIMAL);
        }
        int length = fil.length();
        String filecoin = null;
        if (length < 18) {
            int len = 18 - length;
            for (int i = 0; i < len; i++) {
                fil = "0" + fil;
            }
            filecoin = fileSubstring(fil);
        } else if (length == 18) {
            filecoin = fileSubstring(fil);
        } else if (length > 18) {
            int filMax = length - 18;
            StringBuilder stringBuilder = new StringBuilder(fil);
            stringBuilder.insert(filMax, ".");
            int index = stringBuilder.indexOf(".");
            filecoin = stringBuilder.substring(0, index + 1 + 8);
        }
        return new BigDecimal(filecoin);
    }

    public static String fileSubstring(String fil) {
        String substring = fil.substring(0, 8);
        String filecoin = "0." + substring;
        return filecoin;
    }

    /**
     * 8 位小数 String 转 18 位 fil 单位
     *
     * @param fil
     * @return
     */
    public static String stringTurnBigDecimal(String fil) {
        String filecoin = fil.replace(".", "");
        while (filecoin.length() <= 18) {
            filecoin += "0";
        }
        return filecoin;
    }

    public static String transferFilRaw(String collectAddress, BigDecimal amount, String fromAddress, String privateKey) {

        BigInteger valueInAttoFIL = filToAttoFIL(amount);

        //获取gas
        GasResult gas = getGas(GetGas.builder().from(fromAddress)
                .to(collectAddress)
                .value(valueInAttoFIL).build());
        //获取nonce
        int nonce = getNonce(fromAddress);
        //拼装交易参数
        Transaction transaction = Transaction.builder().from(fromAddress)
                .to(collectAddress)
                .gasFeeCap(gas.getGasFeeCap())
                .gasLimit(gas.getGasLimit().longValue() * 2)
                .gasPremium(gas.getGasPremium())
                .method(0L)
                .nonce((long) nonce)
                .params("")
                .value(valueInAttoFIL.toString()).build();
        log.info("transferFilRaw transaction:{}", JSONObject.toJSONString(transaction));
        return send(transaction, privateKey);
    }

}
