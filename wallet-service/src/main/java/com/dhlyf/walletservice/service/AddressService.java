package com.dhlyf.walletservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dhlyf.walletdao.mybatis.orm.Address;

import java.util.List;

public interface AddressService extends IService<Address> {


    List<Address> queryByIdGreaterThanOrEqual(long lastetId);

    boolean isExist(String address);

}
