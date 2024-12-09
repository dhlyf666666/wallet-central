package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("deposit")
public class Deposit {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long coinId;
    private String symbol;
    //0-主链币交易 1-代币交易
    private Integer transactionType;
    //from地址
    private String fromAddress;
    //to地址
    private String toAddress;
    //金额
    private BigDecimal amount;
    //合约
    private String contract;
    //hash
    private String hash;
    //块高
    private Long blockNumber;
    //状态
    private Integer status;
    //是否归集 0-为归集  1-已归集
    private Integer isCollect;
    //创建时间
    private Date ctime;
    //更新时间
    private Date mtime;

    private String chainType;
}
