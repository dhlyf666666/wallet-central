package com.dhlyf.walletaleo.mq;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.dhlyf.walletaleo.util.AleoUtil;
import com.dhlyf.walletaleo.util.aleo.BlockTransactions;
import com.dhlyf.walletaleo.util.aleo.Output;
import com.dhlyf.walletaleo.util.aleo.ProgramArguments;
import com.dhlyf.walletaleo.util.aleo.Transition;
import com.dhlyf.walletmodel.common.RedisKey;
import com.dhlyf.walletservice.mq.KafkaProducer;
import com.dhlyf.walletservice.mq.Topic;
import com.dhlyf.walletservice.service.AddressService;
import com.dhlyf.walletservice.service.ChainService;
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


@Component
@Slf4j
public class AleoChainTopicConsumer implements ChainTopicConsumerService{

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
    ChainService chainService;

    public void Topic_BLOCK_NUMBER(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "block_number");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));

        try{
            String message = consumerRecord.value();
            long blockNumber = Long.parseLong(message);
            //获取块中所有交易
            List<BlockTransactions> transactions = AleoUtil.getTransactionsList(blockNumber);
            if(!CollectionUtils.isEmpty(transactions)){
                log.info("块高：{}，交易数量：{}", blockNumber, transactions.size());
                //循环交易
                for (BlockTransactions transactionInfo : transactions) {

                    String hash = transactionInfo.getTransaction().getId();
                    if(transactionInfo.getStatus().equals("accepted")){
                        log.info("交易ID：{} 开始处理", hash);
                        List<Transition> transitions = transactionInfo.getTransaction().getExecution().getTransitions();
                        for (Transition transition : transitions) {
                            log.info("交易ID：{} 发送到mq", transition.getId());
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("blockNumber", blockNumber);
                            jsonObject.put("transition", transition);
                            kafkaProducer.sendMessage(topic.getTx(), jsonObject.toJSONString());
                        }
                    }else{
                        log.error("交易ID：{} 开始状态异常 {} ", transactionInfo.getStatus());
                    }

                }
            }else{
                log.info("块{}中不存在交易",blockNumber);
            }
        }catch (Exception e){
            log.error("处理块高异常");
            e.printStackTrace();
            throw e;
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }

//    @KafkaListener(topics = TopicImpl.TX, groupId = "trx-group")
    public void Topic_TX(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "tx");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));

        try{
            String message = consumerRecord.value();
            log.info("开始处理交易数据 message:{}", message);

            JSONObject jsonObjectMessage = JSONObject.parseObject(message);
            Long blockNumber = jsonObjectMessage.getLong("blockNumber");
            Transition transaction = JSONObject.parseObject(jsonObjectMessage.getString("transition"), Transition.class);
            String hasn = transaction.getId();
            String program = transaction.getProgram();
            if(!"credits.aleo".equalsIgnoreCase(program)){
                log.error("交易 {} 不是 credits.aleo 实际是 {} 跳过", hasn, program);
                return;
            }

            String function = transaction.getFunction();
            if(!"transfer_public".equalsIgnoreCase(function)){
                log.error("交易 {} 不是 transfer_public 实际是 {} 跳过", hasn, function);
                return;
            }

            String toAddress = transaction.getInputs().get(0).getValue();
            BigDecimal amount = new BigDecimal(transaction.getInputs().get(1).getValue().replace("u64", ""));
            amount = amount.divide(new BigDecimal("1000000"));

            log.info("交易ID：{} program {} 类型：{}", hasn, program, function);
            if ("credits.aleo".equalsIgnoreCase(program) && "transfer_public".equalsIgnoreCase(function)) {
                try {
                    log.info("交易ID：{} 是credits.aleo交易", hasn);


                    //redis看下合约和地址是否存在
                    String toAddressKey = (String)RedisUtil.get(redisKey.getRedisAddressPrefix() + toAddress);

                    if(StringUtils.hasText(toAddressKey)) {

                        //缓存中有合约有地址，再去库里确认一下
                        boolean addressExist = addressService.isExist(toAddress);
                        if(!addressExist) {
                            log.warn("交易ID：{} 地址：{} 在库里不存在", hasn, toAddress);
                            return;
                        }


                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("txHash", hasn);
                        jsonObject.put("toAddress", toAddress);


                        Output transactionOut = transaction.getOutputs().get(0);
//                        String outStr = transactionOut.getValue();
//                        ProgramArguments programArguments = JSONObject.parseObject(outStr, ProgramArguments.class);


                        String outStr = transactionOut.getValue();
                        outStr = parseOutput(outStr);
                        ProgramArguments programArguments = JSON.parseObject(outStr, ProgramArguments.class);


                        String from = programArguments.getArguments().get(0);

                        if(chainService.getHotWalletAddress().equalsIgnoreCase(from)){
                            log.info("交易ID：{} 充值地址是热钱包地址跳过 {}", hasn, from);
                            return;
                        }

                        jsonObject.put("fromAddress", from);
                        jsonObject.put("amount", amount.stripTrailingZeros().toPlainString());
                        jsonObject.put("symbol", "ALEO");


                        jsonObject.put("transactionType", 0);
                        //合约
                        jsonObject.put("contract", "0");
                        jsonObject.put("blockNumber", blockNumber);

                        kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());
                        log.info("交易ID：{} 类型：{}\t到账地址：{}\t金额：{}", "TRC20", hasn, toAddress, amount.toPlainString());
                    }else{
                        log.info("交易ID：{} 缓存找不到对应地址 {}", hasn, toAddress);
                    }

                } catch (Exception e) {
                    log.error("交易ID：{} 兜底异常：{}", hasn, e.getMessage());
                    e.printStackTrace();
                }
            }else{
                log.warn("交易ID：{} 不是credits.aleo 或者 transfer_public 交易", hasn, program);
            }
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }

    public String parseOutput(String jsonString) {
        // 原始的非标准 JSON 字符串
//        String jsonString = "{ program_id: credits.aleo,  function_name: transfer_public,  arguments: [  aleo189r23h7dng8qf7pw03spjpggpsszc5n5wcvdue09g4suzxv5acpqeyx4tg, aleo14y748xnwz6urrv3zkvydw9q9sat73u282adhalfacf4dj3jxky8sxfaw8r, 9600000u64  ]}";

        // 移除大括号
        String replacedBraces = jsonString.replace("{", "").replace("}", "").trim();

        // 将每个字段用逗号分隔
        String[] fields = replacedBraces.split(",(?=\\s*\\w+\\s*:)");  // 只分割键值对，跳过数组内部的逗号

        // 使用 StringBuilder 构建最终的 JSON 字符串
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");  // 开始大括号

        // 遍历每个字段
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim(); // 移除前后空格

            // 对字段进行键值对分割
            String[] kv = field.split(":");

            if (kv.length == 2) { // 如果是合法的键值对
                String key = kv[0].trim(); // 获取 key
                String value = kv[1].trim(); // 获取 value

                // 检查是否是数组字段
                if (value.startsWith("[")) {
                    jsonBuilder.append("  \"" + key + "\": [\n");

                    // 移除数组的方括号
                    value = value.replace("[", "").replace("]", "").trim();

                    // 解析数组项
                    String[] arrayItems = value.split(",");
                    for (int j = 0; j < arrayItems.length; j++) {
                        jsonBuilder.append("    \"" + arrayItems[j].trim() + "\"");
                        if (j < arrayItems.length - 1) {
                            jsonBuilder.append(",\n");  // 每个数组项之间添加逗号
                        } else {
                            jsonBuilder.append("\n");
                        }
                    }
                    jsonBuilder.append("  ]");
                } else {
                    // 普通键值对，值加上引号
                    jsonBuilder.append("  \"" + key + "\": \"" + value + "\"");
                }

                // 如果不是最后一个字段，添加逗号
                if (i < fields.length - 1) {
                    jsonBuilder.append(",\n");
                } else {
                    jsonBuilder.append("\n");
                }
            }
        }

        jsonBuilder.append("}");  // 结束大括号

        // 打印最终拼接成的 JSON 字符串
//        System.out.println(jsonBuilder.toString());
        String result = jsonBuilder.toString();
        log.info(jsonString);
        log.info(result);
        return result;
    }
}
