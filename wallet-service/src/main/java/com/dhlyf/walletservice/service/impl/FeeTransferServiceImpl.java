package com.dhlyf.walletservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.FeeTransferMapper;
import com.dhlyf.walletdao.mybatis.orm.FeeTransfer;
import com.dhlyf.walletservice.service.FeeTransferService;
import org.springframework.stereotype.Service;

@Service
public class FeeTransferServiceImpl extends ServiceImpl<FeeTransferMapper, FeeTransfer> implements FeeTransferService {
}
