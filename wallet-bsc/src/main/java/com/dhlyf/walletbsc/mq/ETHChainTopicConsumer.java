package com.dhlyf.walletbsc.mq;

import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletbsc.util.EthUtil;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.RedisKey;
import com.dhlyf.walletservice.mq.KafkaProducer;
import com.dhlyf.walletservice.mq.Topic;
import com.dhlyf.walletservice.service.AddressService;
import com.dhlyf.walletservice.service.ChainTopicConsumerService;
import com.dhlyf.walletservice.service.ContractService;
import com.dhlyf.walletsupport.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;


@Component
@Slf4j
public class ETHChainTopicConsumer implements ChainTopicConsumerService{

    @Autowired
    private KafkaProducer kafkaProducer;
    @Autowired
    ContractService contractService;
    @Autowired
    AddressService addressService;
    @Autowired
    Topic topic;
    @Autowired
    RedisKey redisKey;

//    @KafkaListener(topics = Topic.BLOCK_NUMBER, groupId = "eth-group")
    public void Topic_BLOCK_NUMBER(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "block_number");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
        String message = consumerRecord.value();
        try{
            long blockNumber = Long.parseLong(message);
            List<EthBlock.TransactionResult> transactionResults = EthUtil.transactionResult(blockNumber);


            if(!CollectionUtils.isEmpty(transactionResults)){
                log.info("块 {} 中交易数量 {}", blockNumber, transactionResults.size());
                for(EthBlock.TransactionResult transactionResult : transactionResults){
                    EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) transactionResult.get();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("hash", transaction.getHash());
                    jsonObject.put("blockNumber", blockNumber);
                    kafkaProducer.sendMessage(topic.getTx(), jsonObject.toString());
                }
            }else{
                log.warn("块 {} 中不存在交易",blockNumber);
            }

        }catch (Exception e){
            log.error("{} 处理块高异常", message);
            e.printStackTrace();
            throw e;
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }


//    @KafkaListener(topics = Topic.TX, groupId = "eth-group")
    public void Topic_TX(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "tx");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
        String message = consumerRecord.value();
        try{
            log.info("开始处理交易数据 message:{}", message);
            JSONObject mqObject = JSONObject.parseObject(message);
            String hash = mqObject.getString("hash");
            long blockNumber = mqObject.getLong("blockNumber");

            // 通过交易ID获取Transaction
            Transaction transaction = EthUtil.getTransactionByHash(hash);
            EthGetTransactionReceipt transactionReceipt = EthUtil.ethGetTransactionReceipt(hash);

            if (transactionReceipt.getTransactionReceipt().isPresent()) {
                TransactionReceipt receipt = transactionReceipt.getTransactionReceipt().get();
                if ("0x1".equals(receipt.getStatus())) { // 确认交易成功

                    String from = transaction.getFrom();
                    String to = transaction.getTo();
                    BigInteger value = transaction.getValue();

                    if(from.equalsIgnoreCase(EthUtil.getHotWalletAddress())){
                        log.warn("交易ID：{} 是热钱包地址，不处理", hash);
                        return;
                    }

                    // 检查是否为普通ETH转账
                    if (!transaction.getInput().equals("0x")) {
                        // 分析input数据判断是否为ERC20交易
                        if (transaction.getInput().length() >= 138 && transaction.getInput().substring(0, 10).equals("0xa9059cbb")) {
                            // 是ERC20代币的转账交易
                            String tokenTo = "0x" + transaction.getInput().substring(34, 74);
                            BigInteger tokenValue = Numeric.toBigInt(transaction.getInput().substring(74));


                            String contractAddress = to;
                            //redis看下合约和地址是否存在
                            String contractKey = (String) RedisUtil.get(redisKey.getRedisContractPrefix() + contractAddress);
                            String toAddressKey = (String)RedisUtil.get(redisKey.getRedisAddressPrefix() + tokenTo);

                            if(StringUtils.hasText(contractKey) && StringUtils.hasText(toAddressKey)) {

                                //缓存中有合约有地址，再去库里确认一下
                                boolean contractExist = contractService.isExist(contractAddress);
                                boolean addressExist = addressService.isExist(tokenTo);
                                if (!contractExist || !addressExist) {
                                    log.warn("交易ID：{} 合约：{} 地址：{} 在库里不存在 合约存在：{} 地址存在：{}", hash, contractAddress, to, contractExist, addressExist);
                                    return;
                                }
                                Coin coin = JSONObject.parseObject(contractKey,Coin.class);
                                BigDecimal readableTokenValue = new BigDecimal(tokenValue).divide(new BigDecimal(10).pow(coin.getCoinDecimals()));

                                // 构建JSON对象
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("txHash", hash);
                                jsonObject.put("toAddress", tokenTo);
                                jsonObject.put("fromAddress", from);
                                jsonObject.put("amount", readableTokenValue);
                                jsonObject.put("symbol", coin.getCoinName());
                                jsonObject.put("contract", contractAddress);
                                jsonObject.put("transactionType", 1);
                                jsonObject.put("blockNumber", blockNumber);

                                kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());

                                log.info("交易ID：{} 类型：{}\t到账地址：{}\t金额：{}", hash, "TRC20", tokenTo, readableTokenValue);
                            }else{
                                log.warn("交易ID：{} 地址：{} 不存在 {} 合约：{} 不存在 {}", hash, tokenTo, StringUtils.hasText(toAddressKey), contractAddress, StringUtils.hasText(contractKey));
                            }

                        }else{
                            log.error("交易不是erc20转账 跳过:{} ", hash);
                        }
                    } else {

                        String toAddressKey = (String)RedisUtil.get(redisKey.getRedisAddressPrefix() + to);


                        if(StringUtils.hasText(toAddressKey)) {

                            boolean addressExist = addressService.isExist(to);
                            if (!addressExist) {
                                log.warn("交易ID：{} 地址：{} 在库里不存在 ", hash, to);
                                return;
                            }

                            BigDecimal valueEth = new BigDecimal(value).divide(new BigDecimal("1000000000000000000"));

                            // 构建JSON对象
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("txHash", hash);
                            jsonObject.put("toAddress", to);
                            jsonObject.put("fromAddress", from);
                            jsonObject.put("amount", valueEth);
                            jsonObject.put("symbol", "ETH");
                            jsonObject.put("contract", "0");
                            jsonObject.put("transactionType", 0);
                            jsonObject.put("blockNumber", blockNumber);


                            kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());
                            log.info("交易ID：{} 类型：{} 到账地址：{} 金额：{}", hash, "ETH", to, valueEth);
                        }else{
                            log.warn("交易ID：{} 地址：{} 不存在", hash, to);
                        }

                    }
                }else{
                    log.warn(" \"0x1\".equals(receipt.getStatus()) 交易ID：{} 未确认", hash);
                }
            }else{
                log.warn(" transactionReceipt.getTransactionReceipt().isPresent() 交易ID：{} 未确认", hash);
            }

        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }
}
