package com.dhlyf.walletfil.mq;

import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletfil.model.fil.MessageItem;
import com.dhlyf.walletfil.util.FilUtil;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@Slf4j
public class FILChainTopicConsumer implements ChainTopicConsumerService{

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

//    @KafkaListener(topics = Topic.BLOCK_NUMBER, groupId = "fil-group")
    public void Topic_BLOCK_NUMBER(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "block_number");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
        String message = consumerRecord.value();
        try{

            long blockNumber = Long.parseLong(message);
            List<String> cids = FilUtil.getBlockByNumber(blockNumber);


            if(!CollectionUtils.isEmpty(cids)){
                for(String cid : cids){
                    log.info("块高：{}，交易：{}", blockNumber, cid);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cid", cid);
                    jsonObject.put("blockNumber", blockNumber);
                    String data = jsonObject.toString();
//                    log.info("发送交易数据到kafka:{}", data);
                    kafkaProducer.sendMessage(topic.getTx(), data);
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



//    @KafkaListener(topics = Topic.BLOCK_NUMBER, groupId = "fil-group")
//    public void Topic_BLOCK_NUMBER(String message) {
//        MDC.put("logFileName", "block_number");
//        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
//
//        try{
//            long blockNumber = Long.parseLong(message);
//            //获取块中所有交易
//            List<Response.TransactionInfo> transactions = FilUtil.getTransactionsList(blockNumber);
//            log.info("块高：{}，交易数量：{}", blockNumber, transactions.size());
//            //循环交易
//            for (Response.TransactionInfo transactionInfo : transactions) {
//                // 如果hash是byte数组
//                byte[] transactionHashBytes = transactionInfo.getId().toByteArray();
//                // 将byte数组转换为16进制字符串
//                String transactionId = Hex.toHexString(transactionHashBytes);
//
//                log.info("交易ID：{} 开始处理", transactionId);
//
//
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("txHash", transactionId);
//                jsonObject.put("blockNumber", blockNumber);
//                kafkaProducer.sendMessage(Topic.TX, jsonObject.toString());
//
//            }
//        }catch (Exception e){
//            log.error("处理块高异常");
//            e.printStackTrace();
//            throw e;
//        }finally {
//            MDC.remove("logFileName");
//            MDC.remove("uuid");
//        }
//    }


//    @KafkaListener(topics = Topic.TX, groupId = "fil-group")
    public void Topic_TX(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "tx");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));

        try{
            String message = consumerRecord.value();
            log.info("开始处理交易数据 message:{}", message);
            JSONObject mqObject = JSONObject.parseObject(message);
            String cid = mqObject.getString("cid");
            long blockNumber = mqObject.getLong("blockNumber");

            // 通过交易ID获取Transaction
            log.info("cid:{} 开始获取交易信息", cid);
            List<MessageItem> filMessageList = FilUtil.getChainGetBlockMessages(cid);

            if(!CollectionUtils.isEmpty(filMessageList)){
                List<MessageItem> filMessageListCollect = filMessageList.stream().distinct().collect(Collectors.toList());
                for (MessageItem smi: filMessageListCollect){
//                需要校验hash是否交易完成
                    String to = smi.getTo();
                    String hash = smi.getCID().getCid();

                    //redis存在hash
                    if(RedisUtil.exists(redisKey.getRedisHashPrefix()+hash)){
                        log.info("cid:{} 交易已经处理过", hash);
                        continue;
                    }

                    if(StringUtils.hasText(to)){
                        if(to.matches("^f1.*")){
                            String from = smi.getFrom();
                            String toAddressKey = (String)RedisUtil.get(redisKey.getRedisAddressPrefix() + to);

                            if (StringUtils.hasText(to)
                                    && StringUtils.hasText(toAddressKey)
                                    && !from.equalsIgnoreCase(FilUtil.getHotWalletAddress())) {


                                boolean addressExist = addressService.isExist(to);
                                if (!addressExist) {
                                    log.warn("交易ID：{} 地址：{} 在库里不存在 地址存在：{}", hash, to, addressExist);
                                    return;
                                }

                                //如果是热钱包出金  跳过
                                if(from.equalsIgnoreCase(FilUtil.getHotWalletAddress())){
                                    log.warn("交易ID：{} 热钱包地址出金 跳过", hash);
                                    return;
                                }
                                BigDecimal amountFil = new BigDecimal(smi.getValue()).divide(new BigDecimal("1000000000000000000"));

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("txHash", hash);
                                jsonObject.put("toAddress", to);
                                jsonObject.put("fromAddress", from);
                                jsonObject.put("amount", amountFil);
                                jsonObject.put("symbol", "FIL");


                                jsonObject.put("transactionType", 0);
                                //合约
                                jsonObject.put("contract", "0");
                                jsonObject.put("blockNumber", blockNumber);
                                kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());
                                //hash存入redis 24小时过期
                                RedisUtil.set(redisKey.getRedisHashPrefix()+hash, hash, 24*60*60);

                                log.info("交易ID：{} 类型：{}\t到账地址：{}\t金额：{}", hash, "TRX", to, amountFil);
                            }else{
                                log.error("hash:{} 交易错误 错误原因 toHasText:{} to {} 地址不是关注地址{} to地址是热钱包地址:{}",
                                        hash,StringUtils.hasText(to), to, StringUtils.hasText(toAddressKey), !from.equalsIgnoreCase(FilUtil.getHotWalletAddress()));
                            }

                        }else{
                            log.error("cid:{} 交易错误 错误原因 to地址格式不匹配 to:{} hash:{} ", cid, to, hash);
                        }
                    }else{
                        log.error("cid:{} 交易错误 错误原因 to is null hash:{} ", cid, hash);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }
}
