package com.dhlyf.walletservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.DepositMapper;
import com.dhlyf.walletdao.mybatis.orm.Deposit;
import com.dhlyf.walletservice.service.DepositService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepositServiceImpl extends ServiceImpl<DepositMapper, Deposit> implements DepositService {
    @Override
    public List<Deposit> getDepositList(long lastetNumber) {
        Wrapper<Deposit> wrapper = new QueryWrapper<>();
        ((QueryWrapper<Deposit>) wrapper).le("block_number", lastetNumber);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateByRequiresNew(Deposit deposit) {
        baseMapper.updateById(deposit);
    }


    @Override
    public List<Deposit> getByEntity(Deposit deposit) {
        QueryWrapper<Deposit> wrapper = new QueryWrapper<>();
        wrapper.setEntity(deposit);
        return baseMapper.selectList(wrapper);
    }
}
