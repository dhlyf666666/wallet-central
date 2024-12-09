package com.dhlyf.walletdao.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dhlyf.walletdao.mybatis.orm.Account;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper extends BaseMapper<Account> {

    @Select("SELECT * FROM account WHERE id = #{id} FOR UPDATE")
    Account selectByIdForUpdate(Long id);
}
