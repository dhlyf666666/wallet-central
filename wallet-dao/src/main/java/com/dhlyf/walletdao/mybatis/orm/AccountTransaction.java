package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("account_transaction")
public class AccountTransaction {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    //accountid
    private Long accountId;

    private Long coinId;
    private String coinName;
    //refid
    private Long refId;
    //type
    private String type;

    //fromAddress
    private String fromAddress;
    //toAddress
    private String toAddress;

    //amount
    private BigDecimal amount;
    //实际到账数量
    private BigDecimal realAmount;
    //fee
    private BigDecimal fee;

    private String chainType;
}
