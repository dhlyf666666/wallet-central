package com.dhlyf.walletservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.ContractMapper;
import com.dhlyf.walletdao.mybatis.orm.Coin;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CoinType;
import com.dhlyf.walletservice.service.ContractService;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Coin> implements ContractService {

    //根据实体查询唯一结果
    @Override
    public Coin selectOneByEntity(Coin entity) {
        QueryWrapper<Coin> wrapper = new QueryWrapper<>();
        wrapper.setEntity(entity);
        return baseMapper.selectOne(wrapper);
    }

    //判断合约是否存在
    @Override
    public boolean isExist(String address) {
        QueryWrapper<Coin> wrapper = new QueryWrapper<>();
        wrapper.eq("address", address);
        return baseMapper.selectCount(wrapper) > 0;
    }

    //根据合约地址查询合约
    @Override
    public Coin getByAddress(String address) {
        QueryWrapper<Coin> wrapper = new QueryWrapper<>();
        wrapper.eq("address", address);
        return baseMapper.selectOne(wrapper);
    }

    //根据类型查询
    @Override
    public Coin getByType(CoinType type, Chain chain) {
        QueryWrapper<Coin> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type.name()).eq("chain_type", chain.name());
        return baseMapper.selectOne(wrapper);
    }
}
