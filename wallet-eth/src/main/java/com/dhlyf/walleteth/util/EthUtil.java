package com.dhlyf.walleteth.util;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CommonResult;
import com.dhlyf.walletmodel.common.WalletException;
import com.dhlyf.walletmodel.common.WalletResult;
import com.dhlyf.walletmodel.web.req.RequestData;
import com.dhlyf.walletmodel.web.req.cool.CoolWalletFormatParam;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Slf4j
public class EthUtil {


    private static final String rpcAddress = SpringContextUtil.getProperty("eth.rpcAddress");
    private static final String walletAddress = SpringContextUtil.getProperty("eth.walletAddress");
    private static final String walletPrivateKey = SpringContextUtil.getProperty("eth.walletPrivateKey");
    private static final String coolWalletUrl = SpringContextUtil.getProperty("coolwallet.url");
    private static final String etherscanUrl = SpringContextUtil.getProperty("etherscan.url");

    private final static Web3j web3j = Web3j.build(new HttpService(rpcAddress));


    public static long getNowBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<EthBlock.TransactionResult> transactionResult(long blockNumber) {
        try{
            EthBlock blockResponse = web3j.ethGetBlockByNumber(
                            new org.web3j.protocol.core.DefaultBlockParameterNumber(blockNumber), true)
                    .send();
            return blockResponse.getBlock().getTransactions();
        }catch (Exception e){
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    public static Transaction getTransactionByHash(String hash) {
        try{
            Transaction transaction = web3j.ethGetTransactionByHash(hash).send().getResult();
            return transaction;
        }catch (Exception e){
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    public static EthGetTransactionReceipt ethGetTransactionReceipt(String hash) {
        try{
            EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(hash).send();
            return transactionReceipt;
        }catch (Exception e){
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    public static String getHotWalletAddress() {
        return walletAddress;
    }

    public static BigDecimal getBalance(String address) {
        try{
            EthGetBalance getBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            return Convert.fromWei(getBalance.getBalance().toString(), Convert.Unit.ETHER);
        }catch (Exception e){
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    /**
     * 获取账户代币余额
     * @param account 账户地址
     * @param coinAddress 代币地址
     * @return 代币余额 （单位：代币最小单位）
     * @throws IOException
     */
    public BigInteger getBalanceOfCoin(String account, String coinAddress) throws IOException {
        Function balanceOf = new Function("balanceOf",
                Arrays.<Type>asList(new Address(account)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));

        if (coinAddress == null) {
            return null;
        }
        String value = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(account, coinAddress, FunctionEncoder.encode(balanceOf)), DefaultBlockParameterName.PENDING).send().getValue();
        return new BigInteger(value.substring(2), 16);
    }


    public static BigInteger getGasPrice() throws IOException {
        EthGasPrice gasPrice = web3j.ethGasPrice().send();
        return  gasPrice.getGasPrice();
    }

    public static String transferErc20Raw(String contract, Integer coinDecimals, String address, BigDecimal amount) {
        return transferErc20Raw(contract,coinDecimals,address,amount,walletAddress,walletPrivateKey);
    }

    public static String transferErc20Raw(String contract, Integer coinDecimals,
                                          String address, BigDecimal amount, String fromAddress, String privateKey) {

       try{
           EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
                   .sendAsync()
                   .get();
           BigInteger nonce = ethGetTransactionCount.getTransactionCount();

           BigInteger gasPrice = getGasPrice();

           EthRaw ethRaw = new EthRaw();
           ethRaw.setType(1);
           ethRaw.setNonce(nonce);
           ethRaw.setContractAddress(contract);
           ethRaw.setPrivateKey(privateKey);
           ethRaw.setGasPrice(gasPrice);
           ethRaw.setGasLimit(BigInteger.valueOf(100000));

           Function fn = new Function("transfer", Arrays.asList(new Address(address), new Uint256(amountToContractUnit(amount,coinDecimals))), Collections.<TypeReference<?>> emptyList());
           String data = FunctionEncoder.encode(fn);
           ethRaw.setData(data);

           String sign = coolSign(ethRaw);

           EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(sign).sendAsync().get();
           if (ethSendTransaction.hasError()) {
               Response.Error error = ethSendTransaction.getError();
               log.error("Error code  {} Error message {}",error.getCode(), error.getMessage());
               throw new WalletException.Builder()
                       .code(WalletResult.UNKNOWN_ERROR.getCode())
                       .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                       .build();
           } else {
               String transactionHash = ethSendTransaction.getTransactionHash();
               return transactionHash;
           }

       }catch (Exception e){
           log.error(e.getMessage());
           e.printStackTrace();
           throw new WalletException.Builder()
                   .code(WalletResult.UNKNOWN_ERROR.getCode())
                   .msg("Failed to transfer ERC20 due to: " + e.getMessage())
                   .build();
       }

    }

    public static String transferEthRaw(String address, BigDecimal amount) {
        return transferEthRaw(address,amount,walletAddress,walletPrivateKey);
    }


    public static String transferEthRaw(String address, BigDecimal amount, String fromAddress, String privateKey) {
        try{
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            BigInteger gasPrice = getGasPrice();

            EthRaw ethRaw = new EthRaw();
            ethRaw.setType(0);
            ethRaw.setNonce(nonce);
            ethRaw.setToAddress(address);
            ethRaw.setPrivateKey(privateKey);
            ethRaw.setGasPrice(gasPrice);
            ethRaw.setGasLimit(BigInteger.valueOf(60000));
            ethRaw.setValue(ethToWei(amount));
            String sign = coolSign(ethRaw);

            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(sign).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();

            return transactionHash;

        }catch (Exception e){
            log.error("error:{}",e.getMessage());
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    public static String coolSign(EthRaw ethRaw){

        log.info("coolSign btcRaw:{}",ethRaw);
        RequestData requestData = CoolWalletFormatParam.formatParma(ethRaw, Chain.ETH);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);
        log.info("requestCoolWalletParam:{}",requestCoolWalletParam);
        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/eth")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        log.info("coolWalletResult:{}",coolWalletResult);
        if(commonResult.getCode() == 0) {
            String signReulst = (String) commonResult.getData();
            return signReulst;
        }else{
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }


    public static JSONObject getGastracker(){
        String body = HttpUtil.createGet(etherscanUrl + "?module=gastracker&action=gasoracle&apikey=ZCUZRQG13FR6W8MWA2WMWPAUHIHBBI3H84")
                .setHttpProxy("172.20.6.59", 3128)
                .execute().body();
        log.info("getGastracker body:{}",body);
        JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("result");
        return jsonObject;
    }

    private static final BigDecimal WEI_IN_ETH = new BigDecimal("1000000000000000000");

    public static BigDecimal gweiToEth(BigDecimal gwei) {
        return gwei.divide(new BigDecimal("1000000000"));
    }
    // ETH到Wei
    public static BigInteger ethToWei(BigDecimal eth) {
        return eth.multiply(WEI_IN_ETH).toBigInteger();
    }

    // Wei到ETH
    public static BigDecimal weiToEth(BigInteger wei) {
        return new BigDecimal(wei).divide(WEI_IN_ETH);
    }

    // 通用金额到合约最小单位（假定精度为decimals）
    public static BigInteger amountToContractUnit(BigDecimal amount, int decimals) {
        BigDecimal unit = BigDecimal.TEN.pow(decimals);
        return amount.multiply(unit).toBigInteger();
    }

    // 合约最小单位到通用金额
    public static BigDecimal contractUnitToAmount(BigInteger unit, int decimals) {
        BigDecimal decimalUnit = BigDecimal.TEN.pow(decimals);
        return new BigDecimal(unit).divide(decimalUnit);
    }


}
