package com.dhlyf.walletservice.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.dhlfy.financerpcclient.Chain;
import com.dhlfy.financerpcclient.DepositRechargeReq;
import com.dhlfy.financerpcclient.ParamUtil;
import com.dhlfy.financerpcclient.RequestData;
import com.dhlyf.walletdao.mybatis.orm.*;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.AccountTransactionType;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletmodel.common.HttpApplicationType;
import com.dhlyf.walletservice.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class DepositTopicConsumerImpl implements DepositTopicConsumerService {

    @Autowired
    private DepositService depositService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ChainService chainService;

//    private final String financeUrl = SpringContextUtil.getProperty("finance.url");
    private final String financeUrl = "";


    //    @KafkaListener(topics = TopicImpl.DEPOSIT, groupId = "eth-group")
    public void Topic_DEPOSIT(ConsumerRecord<String, String> consumerRecord) {

        String message = consumerRecord.value();
        MDC.put("logFileName", "deposit");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));

        try{
            log.info("开始保存交易数据 message:{}", message);
            //处理交易
            JSONObject jsonObject = JSONObject.parseObject(message);
            Deposit deposit = new Deposit();
            deposit.setTransactionType(jsonObject.getInteger("transactionType"));
            deposit.setSymbol(jsonObject.getString("symbol"));
            deposit.setFromAddress(jsonObject.getString("fromAddress"));
            deposit.setToAddress(jsonObject.getString("toAddress"));
            deposit.setAmount(jsonObject.getBigDecimal("amount"));
            deposit.setContract(jsonObject.getString("contract"));
            deposit.setHash(jsonObject.getString("txHash"));
            deposit.setBlockNumber(jsonObject.getLong("blockNumber"));
            deposit.setStatus(0);
            deposit.setIsCollect(0);
            deposit.setChainType(chainService.getChain());
            if(!deposit.getContract().equals("0")){
                String contractAddress = deposit.getContract();
                Coin coin = contractService.getByAddress(contractAddress);
                deposit.setCoinId(coin.getId());
            }
            log.info("准备插入数据库 deposit:{}", deposit);
            //插入数据库
            boolean saveResult = depositService.save(deposit);
            log.info("交易入库结果 result:{}", saveResult);

            Coin coin = null;
            if(deposit.getTransactionType().compareTo(0) == 0) {
                coin = contractService.getByType(CoinType.main, com.dhlyf.walletmodel.common.Chain.valueOf(chainService.getChain()));
            }else{
                coin = contractService.getByAddress(deposit.getContract());
            }

            Account account = accountService.getAccount(coin.getId(), deposit.getToAddress());
            AccountTransaction accountTransaction = new AccountTransaction();
            accountTransaction.setAccountId(account.getId());
            accountTransaction.setCoinId(deposit.getCoinId());
            accountTransaction.setCoinName(deposit.getSymbol());
            accountTransaction.setType(AccountTransactionType.deposit.name());
            accountTransaction.setFromAddress(deposit.getFromAddress());
            accountTransaction.setToAddress(deposit.getToAddress());
            accountTransaction.setAmount(deposit.getAmount());
            accountTransaction.setRefId(deposit.getId());
//            accountTransaction.setHash(deposit.getHash());
            accountTransaction.setRealAmount(deposit.getAmount());
            accountTransaction.setFee(BigDecimal.ZERO);
            accountTransaction.setChainType(chainService.getChain());
            accountService.transfer(accountTransaction);

            DepositRechargeReq depositRechargeReq = new DepositRechargeReq();
            depositRechargeReq.setAmount(deposit.getAmount());
            depositRechargeReq.setSymbol(deposit.getSymbol());
            depositRechargeReq.setTxid(deposit.getHash());
            depositRechargeReq.setAddress(deposit.getToAddress());
            depositRechargeReq.setFromAddress(deposit.getFromAddress());
            depositRechargeReq.setBlockHeight(deposit.getBlockNumber());
            depositRechargeReq.setTransactionType(deposit.getTransactionType());
            depositRechargeReq.setContractAddress(deposit.getContract());
            RequestData<DepositRechargeReq> requestData = ParamUtil.formatParma(depositRechargeReq, Chain.valueOf(chainService.getChain()));
            log.info("准备调用finance-web服务");

            Notify notify = new Notify();
            notify.setErrorCount(0);
            notify.setStatus(0);
            notify.setUrl(financeUrl + "/deposit/recharge");
            notify.setParams(JSONObject.toJSONString(requestData));
            notify.setApplicationType(HttpApplicationType.JSON.getType());
            notify.setMessage("");
            notify.setChainType(chainService.getChain());
            boolean notifyResult = notifyService.save(notify);
            log.info("异步任务入库结果 result:{}", notifyResult);
        }catch (Exception e){
            log.error("处理交易数据异常", e);
            e.printStackTrace();
            throw e;
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }

    }
}
