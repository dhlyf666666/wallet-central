package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("withdraw")
public class Withdraw {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String coinName;
    //0-主链币交易 1-代币交易
    private Integer transactionType;
    //bizId
    private Long bizId;
    //提现地址
    private String address;
    //提现金额
    private BigDecimal amount;
    //合约
    private String contract;
    //交易hash
    private String hash;
    //状态 0-刚入库 1-上链
    private Integer status;
    //创建时间
    private Date ctime;
    //更新时间
    private Date mtime;

    private String chainType;

}
