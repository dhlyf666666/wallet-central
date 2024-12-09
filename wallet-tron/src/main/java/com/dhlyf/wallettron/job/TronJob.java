package com.dhlyf.wallettron.job;

import com.dhlyf.walletservice.service.CollectService;
import com.dhlyf.walletservice.service.NotifyService;
import com.dhlyf.walletservice.service.TronJobService;
import com.dhlyf.walletservice.service.WithdrawService;
import com.dhlyf.walletsupport.utils.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@EnableScheduling
@Slf4j
public class TronJob {

    @Autowired
    private TronJobService tronJobService;
    @Autowired
    private CollectService collectService;
    @Autowired
    WithdrawService withdrawService;
    @Autowired
    NotifyService notifyService;

    //扫快
    @Scheduled(fixedRate = 60000)
    public void scanBlock() {

        RedissonUtil.withLock("TronJob.scanBlock", () -> {
            MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("开始执行扫块任务");
            tronJobService.scanBlock();
            log.info("处理扫快任务结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }

    //拉取提现记录
    @Scheduled(fixedRate = 60000)
    public void pullWithdraw() {

        RedissonUtil.withLock("TronJob.pullWithdraw", () -> {
            MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("开始执行拉取提现记录任务");
            withdrawService.pullWithdraw();
            log.info("拉取提现记录任务执行结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }

    //推送上链提现
    @Scheduled(fixedRate = 60000)
    public void pushWithdraw() {

        RedissonUtil.withLock("TronJob.pushWithdraw", () -> {
            MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("开始执行通知任务处理");
            withdrawService.pushWithdraw();
            log.info("处理提现任务执行结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }


    //处理提现
    @Scheduled(fixedRate = 60000)
    public void handleWithdraw() {

        RedissonUtil.withLock("TronJob.handleWithdraw", () -> {
            log.info("开始执行处理提现任务");
            withdrawService.handleWithdraw();
            log.info("处理提现任务执行结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }

    //撤销提现
    @Scheduled(fixedRate = 60000)
    public void cancelWithdraw() {

        RedissonUtil.withLock("TronJob.cancelWithdraw", () -> {
            MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("开始执行撤销提现任务");
            withdrawService.cancelWithdraw();
            log.info("撤销提现任务执行结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }

    //通知任务处理
    @Scheduled(fixedRate = 60000)
    public void notifyTask() {

        RedissonUtil.withLock("TronJob.notifyTask", () -> {
            MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("开始执行通知任务处理");
            notifyService.notifyTask();
            log.info("通知任务处理执行结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }

//    充值归集
    @Scheduled(fixedRate = 30*60000)
    public void collectDeposit() {

        RedissonUtil.withLock("TronJob.collectDeposit", () -> {
            MDC.put("uuid", UUID.randomUUID().toString().replaceAll("-", ""));
            log.info("开始执行充值归集任务");
            collectService.collectAccount();
            log.info("充值归集任务执行结束");
            return null;
        }, 10, 10, java.util.concurrent.TimeUnit.SECONDS);

    }
}
