package com.dhlyf.walletbtc.mq;

import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletbtc.util.btc.Bitcoin;
import com.dhlyf.walletbtc.util.btc.BitcoinException;
import com.dhlyf.walletbtc.util.btc.BitcoinRPCClient;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@Slf4j
public class BTCChainTopicConsumer implements ChainTopicConsumerService{

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
    @Autowired
    BitcoinRPCClient rpcClient;
    @Value("${hot.wallet.address}")
    private String hotWalletAddress;

//    @KafkaListener(topics = Topic.BLOCK_NUMBER, groupId = "btc-group")
    public void Topic_BLOCK_NUMBER(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "block_number");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
        String message = consumerRecord.value();
        try {
            Long blockNumber = Long.parseLong(message);

            String blockHash = rpcClient.getBlockHash(blockNumber.intValue());
            //获取区块
            Bitcoin.Block block = rpcClient.getBlock(blockHash);
            List<String> txids = block.tx();
            log.info("获取区块(" + blockNumber + ")交易列表，总交易数：" + txids.size() + "");
            //遍历区块中的交易
            for (String txid : txids) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("txHash", txid);
                jsonObject.put("blockNumber", blockNumber);
                kafkaProducer.sendMessage(topic.getTx(), jsonObject.toString());
            }
        }catch (Exception e){
            log.error("处理区块数据异常：{}", e.getMessage());
            e.printStackTrace();
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }

    }

//    @KafkaListener(topics = Topic.TX, groupId = "btc-group")
    public void Topic_TX(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "tx");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
        String message = consumerRecord.value();
        try{
            log.info("开始处理交易数据 message:{}", message);
            JSONObject mqObject = JSONObject.parseObject(message);
            String txid = mqObject.getString("txHash");
            long blockNumber = mqObject.getLong("blockNumber");

            Bitcoin.RawTransaction transaction = rpcClient.getRawTransaction(txid);
            List<Bitcoin.RawTransaction.Out> outs = transaction.vOut();
            if (outs != null) {
                Set<String> fromAddress = getFromAddress(transaction);
                for (Bitcoin.RawTransaction.Out out : outs) {
                    if (out.scriptPubKey() != null) {
                        String address = out.scriptPubKey().address();
                        if (address != null) {

                            //如果from地址是热钱包地址，不处理
                            if(hotWalletAddress.equalsIgnoreCase(address)){
                                log.warn("交易ID：{} 是热钱包地址，不处理", transaction.txId());
                                return;
                            }

                            if(fromAddress.contains(address)){
                                log.warn("交易ID：{} 是热钱包地址，不处理", transaction.txId());
                                return;
                            }

                            BigDecimal amount = new BigDecimal(out.value());

                            // 判断是否为有效转账
                            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                                // 可能是找零或无效输出
                                log.info("交易ID：{} 可能是找零或无效输出，输出金额：{}", txid, amount);
                                return;
                            }

                            String toAddressKey = (String) RedisUtil.get(redisKey.getRedisAddressPrefix() + address);

                            if (StringUtils.hasText(toAddressKey)) {


                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("txHash", transaction.txId());
                                jsonObject.put("toAddress", address);
                                jsonObject.put("fromAddress", printSet(fromAddress));
                                jsonObject.put("amount", amount.setScale(8, RoundingMode.HALF_UP));
                                jsonObject.put("symbol", "BTC");


                                jsonObject.put("transactionType", 0);
                                //合约
                                jsonObject.put("contract", "0");
                                jsonObject.put("blockNumber", blockNumber);
                                kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());
                                log.info("交易ID：{} 类型：{}\t到账地址：{}\t金额：{}", transaction.txId(), "BTC", address, amount.setScale(8, RoundingMode.HALF_UP));
                            }else{
                                log.warn("交易ID：{} 地址：{} 不存在", transaction.txId(), address);
                            }
                        }else{
                            log.warn("交易ID：{} 地址为空", transaction.txId());
                        }
                    }else{
                        log.warn("交易ID：{} out.scriptPubKey 为空", transaction.txId());
                    }
                }
            }else{
                log.warn("交易ID：{} outs 为空", transaction.txId());
            }
        }catch (Exception e){
            log.error("处理交易数据异常：{}", e.getMessage());
            e.printStackTrace();
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }


    public Set<String> getFromAddress(Bitcoin.RawTransaction transaction) throws BitcoinException {
        // 获取交易的详情
        List<Bitcoin.RawTransaction.In> vins = transaction.vIn();

        Set<String> fromAddresses = new HashSet<>();

        for (Bitcoin.RawTransaction.In vin : vins) {
            // 通过 vin 的 txid 获取引用的交易
            Bitcoin.RawTransaction refTransaction = rpcClient.getRawTransaction(vin.txid());

            // 通过 vout 索引获取引用的输出
            Bitcoin.RawTransaction.Out refOut = refTransaction.vOut().get(vin.vout());

            // 提取引用交易的输出地址（发送方地址）
            String fromAddress = refOut.scriptPubKey().address();
            if (fromAddress != null) {
                fromAddresses.add(fromAddress);
            }
        }

        Set<String> addressSet = new HashSet<>();
        for (String address : fromAddresses) {
            addressSet.add(address);
        }
        return addressSet;
    }

    public String printSet(Set<String> set) {
        StringBuffer stringBuffer = new StringBuffer();
        if (set != null && !set.isEmpty()) {
            for (String s : set) {
                stringBuffer.append(s).append(",");
            }
            // 删除最后一个逗号
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        return stringBuffer.toString();
    }
    private String getFromAddress(List<Bitcoin.RawTransaction.Out> outs, String toAddress) {
        if(outs != null) {
            Bitcoin.RawTransaction.Out out = outs.get(1);
            if(out != null){
                if (out.scriptPubKey() != null) {
                    String address = out.scriptPubKey().address();
                    if(address != null) {
                        if(!address.equalsIgnoreCase(toAddress)){
                            return address;
                        }
                    }
                }
            }
        }
        return "from is null";
    }
}
