package com.dhlyf.walletservice.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.NotifyMapper;
import com.dhlyf.walletdao.mybatis.orm.Notify;
import com.dhlyf.walletmodel.common.HttpApplicationType;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, Notify> implements NotifyService{


    @Autowired
    private ChainService chainService;

    @Override
    public void notifyTask() {
        List<Notify> waitNotifyTask = queryWaitNotifyTask();
        if(!CollectionUtils.isEmpty(waitNotifyTask)){
            for (Notify notify : waitNotifyTask) {
                log.info("开始通知:{} 错误次数: {} ", JSONObject.toJSONString(notify), notify.getErrorCount() >= 10);
                //错误次数大于10次，直接标记为失败
                if(notify.getErrorCount() >= 10){
                    notify.setStatus(3);
                    updateById(notify);
                    continue;
                }
                log.info("准备通知");
                HttpResponse httpResponse = HttpUtil.createPost("http://127.0.0.1:8081" + notify.getUrl())
                        .header("Content-Type", HttpApplicationType.JSON.getType())
                        .body(notify.getParams())
                        .execute();
                log.info("params:{} notifyTask httpResponse:{}", notify.getParams(), httpResponse);
                if(httpResponse.getStatus() == 200){
                    JSONObject jsonObject = JSONObject.parseObject(httpResponse.body());
                    if(jsonObject.getInteger("code") == 0) {
                        notify.setStatus(1);
                        updateById(notify);
                    }else{
                        notify.setStatus(2);
                        notify.setMessage(httpResponse.body());
                        notify.setErrorCount(notify.getErrorCount() + 1);
                        updateById(notify);
                    }
                }else{
                    notify.setStatus(2);
                    notify.setMessage(httpResponse.getStatus()+"");
                    notify.setErrorCount(notify.getErrorCount() + 1);
                    updateById(notify);
                }
            }
        }
    }

    //查询待通知任务
    public List<Notify> queryWaitNotifyTask() {
        LambdaQueryWrapper<Notify> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Notify::getStatus, 0, 2).eq(Notify::getChainType, chainService.getChain());
        return baseMapper.selectList(queryWrapper);
    }


}
