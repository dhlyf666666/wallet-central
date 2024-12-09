package com.dhlyf.walletservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhlyf.walletdao.mybatis.mapper.AddressMapper;
import com.dhlyf.walletdao.mybatis.orm.Address;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletservice.service.AddressService;
import com.dhlyf.walletservice.service.ChainService;
import com.dhlyf.walletservice.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Autowired
    private ChainService chainService;

    @Override
    public List<Address> queryByIdGreaterThanOrEqual(long lastetId) {
        LambdaQueryWrapper<Address> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ge(Address::getId, lastetId).eq(Address::getChainType, chainService.getChain());
        return baseMapper.selectList(queryWrapper);
    }

    //判断某地址是否存在
    @Override
    public boolean isExist(String address) {
        LambdaQueryWrapper<Address> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Address::getAddress, address);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    public Address getByAddress(String address) {
        LambdaQueryWrapper<Address> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Address::getAddress, address);
        Address address1 = baseMapper.selectOne(queryWrapper);
        return address1;
    }
}
