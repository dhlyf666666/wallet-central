package com.dhlyf.walletservice.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlfy.financerpcclient.*;
import com.dhlyf.walletdao.mybatis.mapper.WithdrawMapper;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletdao.mybatis.orm.Withdraw;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletmodel.common.WithdrawStatus;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.ContractService;
import com.dhlyf.walletservice.service.WithdrawService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class WithdrawServiceImpl extends ServiceImpl<WithdrawMapper, Withdraw> implements WithdrawService {


    @Autowired
    private ContractService contractService;
    @Autowired
    private ChainService chainService;

    //根据Withdraw中的属性查询提现信息
    @Override
    public List<Withdraw> findByWithdraw(Withdraw withdraw) {
        QueryWrapper<Withdraw> queryWrapper = new QueryWrapper<>();
        queryWrapper.setEntity(withdraw);
        return baseMapper.selectList(queryWrapper);
    }

    //启动一个新事物
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateByRequiresNew(Withdraw withdraw) {
        this.updateById(withdraw);
    }

    @Override
    public void pullWithdraw() {
        PullWithdrawReq pullWithdrawReq = new PullWithdrawReq();
        pullWithdrawReq.setLastId(0L);

        RequestData requestData = ParamUtil.formatParma(pullWithdrawReq, Chain.valueOf(chainService.getChain()));
        log.info("pullWithdraw param = {}", JSONObject.toJSONString(requestData));
        String body = HttpUtil.createPost("http://127.0.0.1:8081/withdraw/pullWithdraw")
                .body(JSONObject.toJSONString(requestData))
                .execute().body();
        log.info("body:{}", body);
        com.dhlfy.financerpcclient.CommonResult<List<com.dhlfy.financerpcclient.WithdrawVO>> result =
                JSONObject.parseObject(body, new TypeReference<CommonResult<List<WithdrawVO>>>(){}.getType());
        log.info("result1:{}", JSONObject.toJSONString(result));


        if(0 != result.getCode()){
            log.warn("拉取提现异常:{}", result);
        }


        List<com.dhlfy.financerpcclient.WithdrawVO> withdrawList = result.getData();
        if(!CollectionUtils.isEmpty(withdrawList)){
            List<Withdraw> saveWithdrawList = new ArrayList<>();
            for(com.dhlfy.financerpcclient.WithdrawVO bizWithdraw : withdrawList){
                if(isExistBizId(bizWithdraw.getId())){
//                    log.error("提现ID：{} , 提现地址：{} 已经存在跳过", bizWithdraw.getId(), bizWithdraw.getAddress());
                    return;
                }

                Withdraw withdraw = new Withdraw();

                if(0 == bizWithdraw.getTransactionType()){
                    Coin coin = contractService.getByAddress(bizWithdraw.getContractAddress());
                    withdraw.setCoinName(coin.getCoinName());
                }else{
                    Coin coin = contractService.getByType(CoinType.main, com.dhlyf.walletmodel.common.Chain.valueOf(chainService.getChain()));
                    withdraw.setCoinName(coin.getCoinName());
                }

                withdraw.setBizId(bizWithdraw.getId());

                withdraw.setAddress(bizWithdraw.getAddress());
                withdraw.setAmount(bizWithdraw.getRealMoney());
                withdraw.setContract(bizWithdraw.getContractAddress());
                withdraw.setStatus(WithdrawStatus.SAVED.getValue());
                withdraw.setTransactionType(bizWithdraw.getTransactionType());
                withdraw.setChainType(chainService.getChain());
                saveWithdrawList.add(withdraw);
            }
            saveBatch(saveWithdrawList);
        }
    }

    @Override
    public void handleWithdraw() {

        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
        try{

            List<Integer> pedingStatus = new ArrayList<>();
            pedingStatus.add(WithdrawStatus.SAVED.getValue());
            pedingStatus.add(WithdrawStatus.BALANCE_NOT_ENOUGH.getValue());
            //查询提现数据
            LambdaQueryWrapper<Withdraw> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Withdraw::getStatus, pedingStatus).eq(Withdraw::getChainType, chainService.getChain());
            List<Withdraw> withdrawList = list(queryWrapper);
            //判断集合是否为空 不为空遍历
            if (!CollectionUtils.isEmpty(withdrawList)) {
                for (Withdraw withdraw : withdrawList) {

                    MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
                    log.info("提现ID：{} , 提现地址：{} 开始处理", withdraw.getId(), withdraw.getAddress());
                    //看看提现表上是否有Hash
                    if (StringUtils.hasText(withdraw.getHash())) {
                        log.error("提现ID：{} , 提现地址：{} 已经有hash了", withdraw.getId(), withdraw.getAddress());
                        return;
                    }
                    //更新提现状态
                    withdraw.setStatus(1);
                    updateByRequiresNew(withdraw);

                    //上链
                    try {
                        String txid = "";
                        //申请交易
                        if(Integer.valueOf(1).compareTo(withdraw.getTransactionType()) == 0){

                            log.info("提现ID:{} 合约转账 合约地址:{}", withdraw.getId(), withdraw.getContract());
//                                log.error("提现ID：{} , 提现地址：{} 合约地址错误", withdraw.getId(), withdraw.getAddress());
                            Coin entity = new Coin();
                            entity.setAddress(withdraw.getContract());
                            Coin coin = contractService.selectOneByEntity(entity);
                            if(coin == null){
                                log.error("提现ID：{} , 提现地址：{} {} 合约地址错误", withdraw.getId(), withdraw.getAddress(), withdraw.getContract());
                                return;
                            }


                            BigDecimal tokenBalance = chainService.getTokenBalance(withdraw.getAddress(), coin.getAddress());
                            if(tokenBalance.compareTo(withdraw.getAmount()) == -1){
                                log.error("提现ID：{} , 提现地址：{} 提现金额{} 余额{} 余额不足 ",
                                        withdraw.getId(), withdraw.getAddress(), withdraw.getAmount().stripTrailingZeros().toPlainString(), tokenBalance.stripTrailingZeros().toPlainString());
                                withdraw.setHash(txid);
                                withdraw.setStatus(WithdrawStatus.BALANCE_NOT_ENOUGH.getValue());
                                updateByRequiresNew(withdraw);
                                continue;
                            }

                            //合约转账
                            txid = chainService.transferToken(withdraw.getContract(), coin.getCoinDecimals(), withdraw.getAddress(),
                                    withdraw.getAmount());
                        }else if(Integer.valueOf(0).compareTo(withdraw.getTransactionType()) == 0){
                            log.info("提现ID:{} 主链币转账", withdraw.getId());
                            //主链币转账
                            BigDecimal balance = chainService.getBalance(withdraw.getAddress());
                            if(balance.compareTo(withdraw.getAmount()) == -1){
                                log.error("提现ID：{} , 提现地址：{} 提现金额{} 余额{} 余额不足 ",
                                        withdraw.getId(), withdraw.getAddress(), withdraw.getAmount().stripTrailingZeros().toPlainString(), balance.stripTrailingZeros().toPlainString());
//                                withdraw.setHash(txid);
//                                withdraw.setStatus(WithdrawStatus.BALANCE_NOT_ENOUGH.getValue());
//                                updateByRequiresNew(withdraw);
//                                continue;
                            }
                            txid = chainService.transfer(withdraw.getAddress(), withdraw.getAmount());
                        }else{
                            log.error("提现ID：{} , 提现地址：{} 交易类型错误", withdraw.getId(), withdraw.getAddress());
                            continue;
                        }

                        log.info("提现ID：{} , 提现地址：{} 上链成功 txid:{}", withdraw.getId(), withdraw.getAddress(), txid);
                        if(StringUtils.hasText(txid)) {
                            withdraw.setHash(txid);
                            withdraw.setStatus(2);
                            updateByRequiresNew(withdraw);
                        }

                    } catch (Exception e) {
                        log.error("提现ID：{} , 提现地址：{} 上链失败，UUID：{}", withdraw.getId(), withdraw.getAddress(), MDC.get("uuid"), e);
                        e.printStackTrace();
                    }finally {
                        MDC.remove("uuid");
                    }

                }
            }
        }finally {
            MDC.remove("uuid");
        }
    }

    @Override
    public void pushWithdraw() {
        LambdaQueryWrapper<Withdraw> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Withdraw::getStatus, WithdrawStatus.HASHED.getValue()).eq(Withdraw::getChainType, chainService.getChain());
        List<Withdraw> withdrawList = list(queryWrapper);
        if (!CollectionUtils.isEmpty(withdrawList)) {
            for (Withdraw withdraw : withdrawList) {
                Thread.startVirtualThread(() -> {
                    try{
                        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
                        log.info("提现ID：{} , 提现地址：{} 开始推送", withdraw.getId(), withdraw.getAddress());
                        PushWithdrawReq pushWithdrawReq = new PushWithdrawReq();
                        pushWithdrawReq.setBizId(withdraw.getBizId());
                        pushWithdrawReq.setHash(withdraw.getHash());
                        RequestData requestData = ParamUtil.formatParma(pushWithdrawReq, Chain.valueOf(chainService.getChain()));
                        log.info("提现ID：{} , 提现地址：{} 推送参数:{}", withdraw.getId(), withdraw.getAddress(), JSONObject.toJSONString(requestData));
                        String body = HttpUtil.createPost("http://127.0.0.1:8081/withdraw/pushWithdraw")
                                .body(JSONObject.toJSONString(requestData))
                                .execute().body();
                        log.info("提现ID：{} , 提现地址：{} 推送结果:{}", withdraw.getId(), withdraw.getAddress(), body);
                        com.dhlfy.financerpcclient.CommonResult<Boolean> result = JSONObject.parseObject(body, com.dhlfy.financerpcclient.CommonResult.class);
                        if(0 != result.getCode()){
                            log.error("提现ID：{} , 提现地址：{} 推送失败", withdraw.getId(), withdraw.getAddress());
                        }
                        withdraw.setStatus(WithdrawStatus.PUSHED.getValue());
                        updateById(withdraw);
                    }finally {
                        MDC.remove("uuid");
                    }
                });
            }
        }
    }

    @Override
    public void cancelWithdraw() {
        LambdaQueryWrapper<Withdraw> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Withdraw::getStatus, WithdrawStatus.WAIT_CANCEL.getValue()).eq(Withdraw::getChainType, chainService.getChain());
        List<Withdraw> withdrawList = list(queryWrapper);
        if (!CollectionUtils.isEmpty(withdrawList)) {
            for (Withdraw withdraw : withdrawList) {
                Thread.startVirtualThread(() -> {
                    try {
                        MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
                        log.info("提现ID：{} , 提现地址：{} 开始撤销", withdraw.getId(), withdraw.getAddress());
                        CancelWithdrawReq cancelWithdrawReq = new CancelWithdrawReq();
                        cancelWithdrawReq.setId(withdraw.getBizId());
                        RequestData requestData = ParamUtil.formatParma(cancelWithdrawReq, Chain.TRX);
                        log.info("提现ID：{} , 提现地址：{} 撤销参数:{}", withdraw.getId(), withdraw.getAddress(), JSONObject.toJSONString(requestData));
                        String body = HttpUtil.createPost("http://127.0.0.1:8081/withdraw/cancelWithdraw")
                                .body(JSONObject.toJSONString(requestData))
                                .execute().body();
                        log.info("提现ID：{} , 提现地址：{} 撤销结果:{}", withdraw.getId(), withdraw.getAddress(), body);
                        com.dhlfy.financerpcclient.CommonResult<Boolean> result = JSONObject.parseObject(body, com.dhlfy.financerpcclient.CommonResult.class);
                        if (0 != result.getCode()) {
                            log.error("提现ID：{} , 提现地址：{} 撤销失败", withdraw.getId(), withdraw.getAddress());
                        }
                        withdraw.setStatus(WithdrawStatus.CANCEL.getValue());
                        updateById(withdraw);
                    } finally {

                    }
                });
            }
        }
    }



    //bizid对应的业务是否存在
    public boolean isExistBizId(Long bizId) {
        LambdaQueryWrapper<Withdraw> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Withdraw::getBizId, bizId);
        return count(queryWrapper) > 0;
    }
}
