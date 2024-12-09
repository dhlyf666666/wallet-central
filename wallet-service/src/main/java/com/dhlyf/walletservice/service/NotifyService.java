package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Notify;

public interface NotifyService extends IService<Notify> {
    void notifyTask();
}
