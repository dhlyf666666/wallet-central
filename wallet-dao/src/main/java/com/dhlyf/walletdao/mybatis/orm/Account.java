package com.dhlyf.walletdao.mybatis.orm;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("account")
public class Account {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    //coinId
    private Long coinId;
    private String coinName;
    //地址
    private String address;
    //余额
    private BigDecimal balance;
    //创建时间
    private Date ctime;
    //更新时间
    private Date mtime;

    private String chainType;

}
