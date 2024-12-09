package com.dhlyf.wallettron.mq;

import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.RedisKey;
import com.dhlyf.walletservice.mq.KafkaProducer;
import com.dhlyf.walletservice.mq.Topic;
import com.dhlyf.walletservice.service.AddressService;
import com.dhlyf.walletservice.service.ChainTopicConsumerService;
import com.dhlyf.walletservice.service.ContractService;
import com.dhlyf.walletsupport.utils.RedisUtil;
import com.dhlyf.wallettron.util.TronUtil;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tron.trident.abi.TypeDecoder;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.NumericType;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Component
@Slf4j
public class TronChainTopicConsumer implements ChainTopicConsumerService{

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

//    @KafkaListener(topics = TopicImpl.BLOCK_NUMBER, groupId = "trx-group")
    public void Topic_BLOCK_NUMBER(ConsumerRecord<String, String> consumerRecord) {
        MDC.put("logFileName", "block_number");
        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));

        try{
            String message = consumerRecord.value();
            long blockNumber = Long.parseLong(message);
            //获取块中所有交易
            List<Response.TransactionInfo> transactions = TronUtil.getTransactionsList(blockNumber);
            log.info("块高：{}，交易数量：{}", blockNumber, transactions.size());
            //循环交易
            for (Response.TransactionInfo transactionInfo : transactions) {
                // 如果hash是byte数组
                byte[] transactionHashBytes = transactionInfo.getId().toByteArray();
                // 将byte数组转换为16进制字符串
                String transactionId = Hex.toHexString(transactionHashBytes);

                log.info("交易ID：{} 开始处理", transactionId);


                JSONObject jsonObject = new JSONObject();
                jsonObject.put("txHash", transactionId);
                jsonObject.put("blockNumber", blockNumber);
                kafkaProducer.sendMessage(topic.getTx(), jsonObject.toString());

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
            JSONObject mqObject = JSONObject.parseObject(message);
            String transactionId = mqObject.getString("txHash");
            long blockNumber = mqObject.getLong("blockNumber");

            // 通过交易ID获取Transaction
            Chain.Transaction transaction = TronUtil.getTransactionById(transactionId);

            boolean status = transaction.getRet(0).getContractRet().getNumber() == 1;
            if (!status) {
                log.error("交易ID：{} 交易失败 跳过", transactionId);
                return;
            }

            // 合约
            Chain.Transaction.Contract contract = transaction.getRawData().getContract(0);
            // 合约类型
            Chain.Transaction.Contract.ContractType contractType = contract.getType();
            // parameter - 数据|入参
            Any parameter = contract.getParameter();
            // 根据合约类型使用不同的工具进行解码
            // 如果是 触发智能合约 操作
            log.info("交易ID：{} 合约类型：{}", transactionId, contractType);
            if (contractType == Chain.Transaction.Contract.ContractType.TriggerSmartContract) {
                try {
                    log.info("交易ID：{} 是智能合约交易", transactionId);
                    // 解码
                    Contract.TriggerSmartContract triggerSmartContract =
                            parameter.unpack(Contract.TriggerSmartContract.class);
                    // 获取交易详情
                    byte[] fromAddressBs = triggerSmartContract.getOwnerAddress().toByteArray();
                    String fromAddress = Base58Check.bytesToBase58(fromAddressBs);

                    //如果from地址是热钱包地址，不处理
                    if(TronUtil.getHotWalletAddress().equals(fromAddress)){
                        log.warn("交易ID：{} 是热钱包地址，不处理", transactionId);
                        return;
                    }

                    // 合约地址
                    byte[] contractAddressBs = triggerSmartContract.getContractAddress().toByteArray();
                    String contractAddress = Base58Check.bytesToBase58(contractAddressBs);

                    String data = Hex.toHexString(triggerSmartContract.getData().toByteArray());

                    //确认是否是trc20转账
                    if(!data.startsWith("a9059cbb")) {
                        log.warn("交易ID：{} data:{} 不是TRC20转账", transactionId, data);
                        return;
                    }

                    // 收款人地址
                    String toAddress = data.substring(8, 72);
                    // 发送金额
                    String amount = data.substring(72, 136);

                    Address address = TypeDecoder.decodeAddress(toAddress);
                    NumericType numericType = TypeDecoder.decodeNumeric(amount, Uint256.class);
                    BigDecimal transferAmount = new BigDecimal(numericType.getValue());
                    String to = address.getValue();

                    //redis看下合约和地址是否存在
                    String contractKey = (String) RedisUtil.get(redisKey.getRedisContractPrefix() + contractAddress);
                    String toAddressKey = (String)RedisUtil.get(redisKey.getRedisAddressPrefix() + to);

                    if(StringUtils.hasText(contractKey) && StringUtils.hasText(toAddressKey)) {

                        //缓存中有合约有地址，再去库里确认一下
                        boolean contractExist = contractService.isExist(contractAddress);
                        boolean addressExist = addressService.isExist(to);
                        if(!contractExist || !addressExist) {
                            log.warn("交易ID：{} 合约：{} 地址：{} 在库里不存在 合约存在：{} 地址存在：{}", transactionId, contractAddress, to, contractExist, addressExist);
                            return;
                        }


                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("txHash", transactionId);
                        jsonObject.put("toAddress", to);
                        jsonObject.put("fromAddress", fromAddress);

//                        com.kpay.tronwallet.model.mybatis.orm.Address dbAddress = JSONObject.parseObject(toAddressKey, com.kpay.tronwallet.model.mybatis.orm.Address.class);

                        Coin contract1 = JSONObject.parseObject(contractKey, Coin.class);
                        //真实价格要根据合约的精度处理
                        BigDecimal realAmount = transferAmount.divide(new BigDecimal(Math.pow(10, contract1.getCoinDecimals())));
                        jsonObject.put("amount", realAmount);
                        jsonObject.put("symbol", contract1.getCoinName());

                        jsonObject.put("transactionType", 1);
                        //合约
                        jsonObject.put("contract", contractAddress);
                        jsonObject.put("blockNumber", blockNumber);
                        kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());
                        log.info("交易ID：{} 类型：{}\t到账地址：{}\t金额：{}", "TRC20", transactionId, to, transferAmount);
                    }else{
                        log.warn("交易ID：{} 合约：{} 地址：{} 不存在", transactionId, contractAddress, to);
                    }

                } catch (Exception e) {
                    log.error("交易ID：{} 兜底异常：{}", transactionId, e.getMessage());
                    e.printStackTrace();
                }
            }
            // 如果是trx
            else if (contractType == Chain.Transaction.Contract.ContractType.TransferContract) {
                try {
                    log.info("交易ID：{} 是TRX交易", transactionId);
                    Contract.TransferContract unpack = parameter.unpack(Contract.TransferContract.class);

                    //获取转账hash
                    String txHash = transactionId;
                    //到账地址
                    ByteString toAddressBs = unpack.getToAddress();
                    String toAddress = Base58Check.bytesToBase58(toAddressBs.toByteArray());
                    // 转账(发起人)地址
                    ByteString fromAddressBs = unpack.getOwnerAddress();
                    String fromAddress = Base58Check.bytesToBase58(fromAddressBs.toByteArray());
                    //获取转账金额
                    long amount = unpack.getAmount();
                    BigDecimal amountTrx = new BigDecimal(amount).divide(new BigDecimal(1_000_000));


                    //如果from地址是热钱包地址，不处理
                    if(TronUtil.getHotWalletAddress().equals(fromAddress)){
                        log.warn("交易ID：{} 是热钱包地址，不处理", transactionId);
                        return;
                    }

                    //redis里面看下地址是否存在
                    String toAddressKey = (String) RedisUtil.get(redisKey.getRedisAddressPrefix() + toAddress);
                    if(StringUtils.hasText(toAddressKey)) {

                        //缓存中有地址，再去库里确认一下
                        boolean addressExist = addressService.isExist(toAddress);
                        if(!addressExist) {
                            log.warn("交易ID：{} 地址：{} 在库里不存在", transactionId, toAddress);
                            return;
                        }

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("txHash", txHash);
                        jsonObject.put("toAddress", toAddress);
                        jsonObject.put("fromAddress", fromAddress);
                        jsonObject.put("amount", amountTrx);
                        jsonObject.put("symbol", "TRX");

//                        com.kpay.tronwallet.model.mybatis.orm.Address dbAddress = JSONObject.parseObject(toAddressKey, com.kpay.tronwallet.model.mybatis.orm.Address.class);

                        jsonObject.put("transactionType", 0);
                        //合约
                        jsonObject.put("contract", "0");
                        jsonObject.put("blockNumber", blockNumber);
                        kafkaProducer.sendMessage(topic.getDeposit(), jsonObject.toString());
                        log.info("交易ID：{} 类型：{}\t到账地址：{}\t金额：{}", transactionId, "TRX", toAddress, amountTrx);
                    }else{
                        log.warn("交易ID：{} 地址：{} 不存在", transactionId, toAddress);
                    }


                } catch (InvalidProtocolBufferException e) {
                    log.error("unpack解包异常");
                    throw new RuntimeException(e);
                }
            }else {
                log.warn("交易ID：{} 类型：{} 不处理", transactionId, contractType);
            }
        }finally {
            MDC.remove("logFileName");
            MDC.remove("uuid");
        }
    }
}
