package com.dhlyf.wallettron.util;


import cn.hutool.core.codec.Base58;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.CommonResult;
import com.dhlyf.walletmodel.web.req.RequestData;
import com.dhlyf.walletmodel.web.req.cool.CoolWalletFormatParam;
import com.dhlyf.walletmodel.web.req.cool.Trc20Raw;
import com.dhlyf.walletmodel.web.req.cool.TrxRaw;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.util.StringUtils;
import org.tron.trident.abi.FunctionEncoder;
import org.tron.trident.abi.TypeReference;
import org.tron.trident.abi.datatypes.*;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.tron.trident.core.utils.Sha256Hash.hash;

@Slf4j
public class TronUtil {

//    private static final String GRPC_ENDPOINT = "38.55.19.14:50051";
//    private static final String SOLIDITY_GRPC_ENDPOINT = "38.55.19.14:50052";

//    private static final String GRPC_ENDPOINT = "127.0.0.1:50051";
//    private static final String SOLIDITY_GRPC_ENDPOINT = "127.0.0.1:50052";

//    private static final ApiWrapper client = ApiWrapper.ofMainnet(
//            "ce29aabbcc96e58ea38fc29be6c68dfd2a14e498644ea4f1367f2b3b36445c6d"
//            ,"cf83ec51-156c-41cc-870b-77144e26c1b8");

//    private static String tronUrl = "https://api.shasta.trongrid.io";
    private static String tronHttpUrl = SpringContextUtil.getProperty("tron.httpUrl");
    private static String tronSolidityUrl = SpringContextUtil.getProperty("tron.solidityUrl");

    private static String trongridUrl = SpringContextUtil.getProperty("tron.trongrid.url");

    private static final String GRPC_ENDPOINT = SpringContextUtil.getProperty("tronnode.grpc.endpoint");
    private static final String SOLIDITY_GRPC_ENDPOINT = SpringContextUtil.getProperty("tronnode.solidity.grpc.endpoint");
    private static final ApiWrapper client = new ApiWrapper(GRPC_ENDPOINT, SOLIDITY_GRPC_ENDPOINT, "ce29aabbcc96e58ea38fc29be6c68dfd2a14e498644ea4f1367f2b3b36445c6d");

    private static final String coolWalletUrl = SpringContextUtil.getProperty("coolwallet.url");


    private static final String hotWalletAddress = SpringContextUtil.getProperty("hot.wallet.address");

    private static final String hotWalletPrivateKey = SpringContextUtil.getProperty("hot.wallet.privatekey");



    /**
     * trx精度  1 trx = 1000000 sun
     */
    private static BigDecimal trxDecimal = new BigDecimal("1000000");

    public static Chain.Block getNowBlock() throws IllegalException {
        return client.getNowBlock();
    }

    //查询账户资源
    public static Response.AccountResourceMessage getAccountResource(String address) {
        try {
            Response.AccountResourceMessage accountResource = client.getAccountResource(address);
            log.info("accountResource:{}", accountResource);
            return accountResource;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取最新区块编号
    public static long getNowBlockNumber() {
        //获取最新块
        Chain.Block block = null;
        try {
            block = getNowBlock();
            return block.getBlockHeader().getRawData().getNumber();
        } catch (IllegalException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //获取块中所有交易
    public static List<Response.TransactionInfo> getTransactionsList(long number) {
        Response.TransactionInfoList block = null;
        try {
            block = client.getTransactionInfoByBlockNum(number);
        } catch (Exception e) {
            log.error("getTransactionsList {} error: {}", number, e.getMessage());
            throw new RuntimeException(e);
        }
        return block.getTransactionInfoList();
    }

    public static String getTransactionId(Chain.Transaction transaction) {
        byte[] bytes = ApiWrapper.calculateTransactionHash(transaction);
        return Hex.toHexString(bytes);
    }

    public static Chain.Transaction getTransactionById(String transactionId) {
        // 通过交易ID获取Transaction
        try {
            return client.getTransactionById(transactionId);
        } catch (IllegalException e) {
            throw new RuntimeException(e);
        }
    }

    //getTransactionByHash
    public static Chain.Transaction getTransactionByHash(String hash) {
        // 通过交易ID获取Transaction
        try {
            return client.getTransactionById(hash);
        } catch (IllegalException e) {
            throw new RuntimeException(e);
        }
    }

    //trc20转账
    public static String transferTrc20(String from, String to, String contractAddress, BigDecimal amount, String privateKey) {

        // 根据合约地址获取合约信息
        org.tron.trident.core.contract.Contract contract = client.getContract(contractAddress);
        // 构造trc20合约
        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey);

        Trc20Contract trc20Contract = new Trc20Contract(contract, from, wrapper);

        // 精度
        int decimals = trc20Contract.decimals().intValue();
        // 手续费
        long feeLimit = Convert.toSun("50", Convert.Unit.TRX).longValue();
        // 实际转账金额（trc20不可分割单位）
        BigInteger transferAmount = amount
                .multiply(BigDecimal.TEN.pow(decimals))
                .toBigInteger();

        Function transfer = new Function("transfer",
                Arrays.asList(new Address(to),
                        new Uint256(transferAmount)),
                Arrays.asList(new TypeReference<Bool>() {
                }));

        TransactionBuilder builder = client.triggerCall(from, contractAddress, transfer);
        builder.setFeeLimit(feeLimit);
        builder.setMemo("备注");

        Chain.Transaction signedTxn = client.signTransaction(builder.build());
        String txid = client.broadcastTransaction(signedTxn);
        return txid;
    }

    public void test(){

    }

    public static String getContractName(String contractAddress) {
        Trc20Contract trc20Contract = new Trc20Contract(client.getContract(contractAddress), "THPvaUhoh2Qn2y9THCZML3H815hhFhn5YC", client);
        String name = trc20Contract.name();
        return name;
    }

    public static int getContractDecimals(String contractAddress) {

        Trc20Contract trc20Contract = new Trc20Contract(client.getContract(contractAddress), "THPvaUhoh2Qn2y9THCZML3H815hhFhn5YC", client);
        BigInteger decimals = trc20Contract.decimals();
        return decimals.intValue();
    }


    //获取trx余额
    public static BigDecimal getTrxBalance(String address) {
        long amount = client.getAccount(address).getBalance();
        return Convert.fromSun(new BigDecimal(amount), Convert.Unit.TRX);
    }

    //获取trc20余额
    public static BigDecimal getTrc20Balance(String address, String contractAddress) {
        log.info("address:{},contractAddress:{}", address, contractAddress);
        Trc20Contract trc20Contract = new Trc20Contract(client.getContract(contractAddress), address, client);
        BigInteger balance = trc20Contract.balanceOf(address);
        int decimals = trc20Contract.decimals().intValue();
        return new BigDecimal(balance).divide(BigDecimal.TEN.pow(decimals));
    }

    /**
     * 转换成hex地址
     *
     * @param address
     * @return
     */
    public static String toHexAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("传入的地址不可为空");
        }
        if (!address.startsWith("T")) {
            throw new IllegalArgumentException("传入地址不合法:" + address);
        }
        return Hex.toHexString(decodeFromBase58Check(address));
    }

    public static byte[] decodeFromBase58Check(String addressBase58) {
        try {
            byte[] address = decode58Check(addressBase58);
            if (!addressValid(address)) {
                return null;
            }
            return address;
        } catch (Throwable t) {
            log.error(String.format("decodeFromBase58Check-error:" + addressBase58), t);
        }
        return null;
    }

    static int ADDRESS_SIZE = 21;
    private static byte addressPreFixByte = (byte) 0x41;
    private static boolean addressValid(byte[] address) {
        if (address.length == 0) {
            return false;
        }
        if (address.length != ADDRESS_SIZE) {
            return false;
        }
        byte preFixbyte = address[0];
        return preFixbyte == addressPreFixByte;
        // Other rule;
    }

    private static byte[] decode58Check(String input) throws Exception {
        log.info("-----------decode58Check input:{}", input);
        byte[] decodeCheck = Base58.decode(input);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = hash(true, decodeData);
        byte[] hash1 = hash(true, hash0);
        if (hash1[0] == decodeCheck[decodeData.length] && hash1[1] == decodeCheck[decodeData.length + 1]
                && hash1[2] == decodeCheck[decodeData.length + 2] && hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    /**
     * Calculates the SHA-256 hash of the given bytes.
     *
     * @param input the bytes to hash
     * @return the hash (in big-endian order)
     */
    public static byte[] hash(boolean isSha256, byte[] input) throws NoSuchAlgorithmException {
        return hash(isSha256, input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range.
     *
     * @param input  the array containing the bytes to hash
     * @param offset the offset within the array of the bytes to hash
     * @param length the number of bytes to hash
     * @return the hash (in big-endian order)
     */
    public static byte[] hash(boolean isSha256, byte[] input, int offset, int length) throws NoSuchAlgorithmException {
        if (isSha256) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input, offset, length);
            return digest.digest();
        } else {
            SM3Digest digest = new SM3Digest();
            digest.update(input, offset, length);
            byte[] eHash = new byte[digest.getDigestSize()];
            digest.doFinal(eHash, 0);
            return eHash;
        }
    }

    public static String transferTrc20Raw(String contractAddress, String toAddress,
                                          BigDecimal amount, Integer decimal,
                                          String fromAddress, String privateKey) {
        JSONObject param = new JSONObject();
//        param.put("contract_address", toHexAddress(contractAddress));
        param.put("contract_address", contractAddress);
        param.put("function_selector", "transfer(address,uint256)");
        List<Type> inputParameters = new ArrayList<>();
//        inputParameters.add(new Address(toHexAddress(toAddress).substring(2)));
        inputParameters.add(new Address(toAddress));
        BigInteger amountInteger = convertToMinimalUnit(amount, decimal);
        log.info("amount:{} amountInteger:{} decimal:{} ",amount, amountInteger, decimal);
        inputParameters.add(new Uint256(amountInteger));
        String parameter = FunctionEncoder.encodeConstructor(inputParameters);
        param.put("parameter", parameter);

//        param.put("owner_address", toHexAddress(hotWalletAddress));
        param.put("owner_address", fromAddress);
        param.put("visible",true);
//        param.put("visible",true);
        param.put("call_value", 0);
        param.put("fee_limit", 50000000L);
        String url = trongridUrl + "/wallet/triggersmartcontract";
        String paramStr = param.toJSONString();
        log.info("transferTrc20Raw triggerconstantcontract url:{} param:{}",url, paramStr);
        String createtransactionResult = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .setHttpProxy("172.20.6.59", 3128)
                .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
//                .header("TRON-PRO-API-KEY","cf83ec51-156c-41cc-870b-77144e26c1b8")
                .body(paramStr)
                .execute().body();
        log.info("transferTrc20Raw triggerconstantcontract result:{}",createtransactionResult);
        JSONObject resultJson = JSONObject.parseObject(createtransactionResult);
//        log.info("transferTrc20Raw transaction:{}",resultJson.toJSONString());
        JSONObject transaction = resultJson.getJSONObject("transaction");
        if(transaction == null){
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), resultJson.toJSONString());
            throw new RuntimeException(resultJson.toJSONString());
        }

        Trc20Raw trc20Raw = JSONObject.parseObject(transaction.toJSONString(), Trc20Raw.class);
        trc20Raw.setPrivateKey(privateKey);
        RequestData requestData = CoolWalletFormatParam.formatParma(trc20Raw, com.dhlyf.walletmodel.common.Chain.TRX);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);
        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/trc20")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        if(commonResult.getCode() == 0){
            String signReulst = (String) commonResult.getData();
            JSONObject jsonObjectGB = new JSONObject();
            jsonObjectGB.put("transaction", signReulst);

            String broadcasthexParam = jsonObjectGB.toJSONString();
            log.info("transferTrc20Raw broadcasthexParam:{}",broadcasthexParam);
            String broadcasthexResult = HttpUtil.createPost(trongridUrl + "/wallet/broadcasthex")
                    .setHttpProxy("172.20.6.59", 3128)
                    .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
                    .header("Content-Type", "application/json")
                    .body(broadcasthexParam)
                    .execute().body();


            JSONObject transationCompelet = JSONObject.parseObject(broadcasthexResult);
            log.info("transferTrc20Raw broadcasthexResult:{}",transationCompelet.toJSONString());
            if (transationCompelet.getBoolean("result")) {
                return transationCompelet.getString("txid");
            } else {
                log.error(String.format("签名交易失败:{}",transationCompelet.toJSONString()));
                throw new RuntimeException(transationCompelet.toJSONString());
            }
        }else{
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), coolWalletResult);
            throw new RuntimeException(coolWalletResult);
        }
    }

    public static String transferTrc20Raw(String contractAddress, String toAddress, BigDecimal amount, Integer decimal) {

        JSONObject param = new JSONObject();
//        param.put("contract_address", toHexAddress(contractAddress));
        param.put("contract_address", contractAddress);
        param.put("function_selector", "transfer(address,uint256)");
        List<Type> inputParameters = new ArrayList<>();
//        inputParameters.add(new Address(toHexAddress(toAddress).substring(2)));
        inputParameters.add(new Address(toAddress));
        BigInteger amountInteger = convertToMinimalUnit(amount, decimal);
        log.info("amount:{} amountInteger:{} decimal:{} ",amount, amountInteger, decimal);
        inputParameters.add(new Uint256(amountInteger));
        String parameter = FunctionEncoder.encodeConstructor(inputParameters);
        param.put("parameter", parameter);

//        param.put("owner_address", toHexAddress(hotWalletAddress));
        param.put("owner_address", hotWalletAddress);
        param.put("visible",true);
//        param.put("visible",true);
        param.put("call_value", 0);
        param.put("fee_limit", 50000000L);
        String url = trongridUrl + "/wallet/triggersmartcontract";
        String paramStr = param.toJSONString();
        log.info("transferTrc20Raw triggerconstantcontract url:{} param:{}",url, paramStr);
        String createtransactionResult = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .setHttpProxy("172.20.6.59", 3128)
                .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
//                .header("TRON-PRO-API-KEY","cf83ec51-156c-41cc-870b-77144e26c1b8")
                .body(paramStr)
                .execute().body();
        log.info("transferTrc20Raw triggerconstantcontract result:{}",createtransactionResult);
        JSONObject resultJson = JSONObject.parseObject(createtransactionResult);
//        log.info("transferTrc20Raw transaction:{}",resultJson.toJSONString());
        JSONObject transaction = resultJson.getJSONObject("transaction");
        if(transaction == null){
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), resultJson.toJSONString());
            throw new RuntimeException(resultJson.toJSONString());
        }

        Trc20Raw trc20Raw = JSONObject.parseObject(transaction.toJSONString(), Trc20Raw.class);
        trc20Raw.setPrivateKey(hotWalletPrivateKey);
        RequestData requestData = CoolWalletFormatParam.formatParma(trc20Raw, com.dhlyf.walletmodel.common.Chain.TRX);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);
        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/trc20")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        if(commonResult.getCode() == 0){
            String signReulst = (String) commonResult.getData();
            JSONObject jsonObjectGB = new JSONObject();
            jsonObjectGB.put("transaction", signReulst);

            String broadcasthexParam = jsonObjectGB.toJSONString();
            log.info("transferTrc20Raw broadcasthexParam:{}",broadcasthexParam);
            String broadcasthexResult = HttpUtil.createPost(trongridUrl + "/wallet/broadcasthex")
                    .setHttpProxy("172.20.6.59", 3128)
                    .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
                    .header("Content-Type", "application/json")
                    .body(broadcasthexParam)
                    .execute().body();


            JSONObject transationCompelet = JSONObject.parseObject(broadcasthexResult);
            log.info("transferTrc20Raw broadcasthexResult:{}",transationCompelet.toJSONString());
            if (transationCompelet.getBoolean("result")) {
                return transationCompelet.getString("txid");
            } else {
                log.error(String.format("签名交易失败:{}",transationCompelet.toJSONString()));
                throw new RuntimeException(transationCompelet.toJSONString());
            }
        }else{
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), coolWalletResult);
            throw new RuntimeException(coolWalletResult);
        }

    }


//    {"visible":true,"txID":"e59e85f372087ebfe77edda626ff59c89193b9f3fb0bf9109dcddbad5d1fe70b","raw_data":{"contract":[{"parameter":{"value":{"amount":1000,"owner_address":"TZ4UXDV5ZhNW7fb2AMSbgfAEZ7hWsnYS2g","to_address":"TPswDDCAWhJAZGdHPidFg5nEf8TkNToDX1"},"type_url":"type.googleapis.com/protocol.TransferContract"},"type":"TransferContract"}],"ref_block_bytes":"0ea7","ref_block_hash":"87311f50b8c18de3","expiration":1713302559000,"timestamp":1713302501668},"raw_data_hex":"0a020ea7220887311f50b8c18de34098b2a9c6ee315a66080112620a2d747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e5472616e73666572436f6e747261637412310a1541fd49eda0f23ff7ec1d03b52c3a45991c24cd440e12154198927ffb9f554dc4a453c64b2e553a02d6df514b18e80770a4f2a5c6ee31"}

    public static String transferTrxRaw(String toAddress, BigDecimal amount, String fromAddress, String privateKey) {
        String url = trongridUrl + "/wallet/createtransaction";
        JSONObject param = new JSONObject();
        param.put("owner_address",fromAddress);
        param.put("to_address",toAddress);
        param.put("amount",amount.multiply(trxDecimal).toBigInteger());
        param.put("visible",true);
        log.info("transferTrxRaw param:{}",param.toJSONString());
        String createtransactionResult = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .setHttpProxy("172.20.6.59", 3128)
                .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
//                .header("TRON-PRO-API-KEY","cf83ec51-156c-41cc-870b-77144e26c1b8")
                .body(param.toJSONString())
                .execute().body();
        JSONObject resultJson = JSONObject.parseObject(createtransactionResult);
        JSONObject rawData = resultJson.getJSONObject("raw_data");
        if(rawData == null){
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), resultJson.toJSONString());
            throw new RuntimeException(resultJson.toJSONString());
        }

        TrxRaw trxRaw = JSONObject.parseObject(resultJson.toJSONString(), TrxRaw.class);
        trxRaw.setPrivateKey(privateKey);
        RequestData requestData = CoolWalletFormatParam.formatParma(trxRaw, com.dhlyf.walletmodel.common.Chain.TRX);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);

        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/trx")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        log.info("transferTrxRaw coolWalletParam:{} coolWalletResult:{}",requestCoolWalletParam, coolWalletResult);

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        if(commonResult.getCode() == 0){
            String signReulst = (String) commonResult.getData();
            JSONObject jsonObjectGB = new JSONObject();
            jsonObjectGB.put("transaction", signReulst);

            String broadcasthexResult = HttpUtil.createPost(trongridUrl + "/wallet/broadcasthex")
                    .setHttpProxy("172.20.6.59", 3128)
                    .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
                    .header("Content-Type", "application/json")
                    .body(jsonObjectGB.toJSONString())
                    .execute().body();


            JSONObject transationCompelet = JSONObject.parseObject(broadcasthexResult);
            log.info("transferTrxRaw broadcasthexResult:{}",transationCompelet.toJSONString());
            if (transationCompelet.getBoolean("result")) {
                return transationCompelet.getString("txid");
            } else {
                log.error(String.format("签名交易失败:{}",transationCompelet.toJSONString()));
                throw new RuntimeException(transationCompelet.toJSONString());
            }
        }else{
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), coolWalletResult);
            throw new RuntimeException(coolWalletResult);
        }

    }

    public static String transferTrxRaw(String toAddress, BigDecimal amount) {
        String url = trongridUrl + "/wallet/createtransaction";
        JSONObject param = new JSONObject();
        param.put("owner_address",hotWalletAddress);
        param.put("to_address",toAddress);
        param.put("amount",amount.multiply(trxDecimal).toBigInteger());
        param.put("visible",true);
        log.info("transferTrxRaw param:{}",param.toJSONString());
        String createtransactionResult = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .setHttpProxy("172.20.6.59", 3128)
                .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
//                .header("TRON-PRO-API-KEY","cf83ec51-156c-41cc-870b-77144e26c1b8")
                .body(param.toJSONString())
                .execute().body();
        JSONObject resultJson = JSONObject.parseObject(createtransactionResult);
        JSONObject rawData = resultJson.getJSONObject("raw_data");
        if(rawData == null){
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), resultJson.toJSONString());
            throw new RuntimeException(resultJson.toJSONString());
        }

        TrxRaw trxRaw = JSONObject.parseObject(resultJson.toJSONString(), TrxRaw.class);
        trxRaw.setPrivateKey(hotWalletPrivateKey);
        RequestData requestData = CoolWalletFormatParam.formatParma(trxRaw, com.dhlyf.walletmodel.common.Chain.TRX);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);

        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/trx")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        log.info("transferTrxRaw coolWalletParam:{} coolWalletResult:{}",requestCoolWalletParam, coolWalletResult);

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        if(commonResult.getCode() == 0){
            String signReulst = (String) commonResult.getData();
            JSONObject jsonObjectGB = new JSONObject();
            jsonObjectGB.put("transaction", signReulst);

            String broadcasthexResult = HttpUtil.createPost(trongridUrl + "/wallet/broadcasthex")
                    .setHttpProxy("172.20.6.59", 3128)
                    .header("TRON-PRO-API-KEY","a8a8cdd6-b471-4103-a0d9-0e5a5eb55dbe")
                    .header("Content-Type", "application/json")
                    .body(jsonObjectGB.toJSONString())
                    .execute().body();


            JSONObject transationCompelet = JSONObject.parseObject(broadcasthexResult);
            log.info("transferTrxRaw broadcasthexResult:{}",transationCompelet.toJSONString());
            if (transationCompelet.getBoolean("result")) {
                return transationCompelet.getString("txid");
            } else {
                log.error(String.format("签名交易失败:{}",transationCompelet.toJSONString()));
                throw new RuntimeException(transationCompelet.toJSONString());
            }
        }else{
            log.error("toAddress:{} amount:{} transferTrxRaw error:{}",toAddress, amount.stripTrailingZeros().toPlainString(), coolWalletResult);
            throw new RuntimeException(coolWalletResult);
        }
    }

    //创建一个地址
    public static KeyPair createAddress() {
        KeyPair keyPair = KeyPair.generate();
        return keyPair;
    }

    public static BigInteger convertToMinimalUnit(BigDecimal amount, int decimal) {
        // 创建基数（10的decimal次方）
        BigDecimal base = BigDecimal.TEN.pow(decimal);

        // 乘以基数并转换成BigInteger
        return amount.multiply(base).toBigInteger();
    }

    public static String getHotWalletAddress() {
        return hotWalletAddress;
    }

    //创建一个地址
    public static void main(String[] args) throws IllegalException {
//        KeyPair keyPair = KeyPair.generate();
//        System.out.println(keyPair.toHexAddress());
//        System.out.println(keyPair.toPrivateKey());
//        System.out.println(keyPair.toPublicKey());
//        System.out.println(keyPair.toBase58CheckAddress());
        transferTrc20Raw("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        ,"TM3Hsgd6mWJnprsTs9kBytv7n3zVZaEtmk",new BigDecimal("0.01"),6);
//        System.out.printf(getTrc20Balance("TTnZEHXa4evLvKGTjkKwoj5aWg15xKhbNL","0xdAC17F958D2ee523a2206206994597C13D831ec7").toPlainString());
    }

}
