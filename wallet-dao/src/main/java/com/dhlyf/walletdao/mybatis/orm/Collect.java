package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("collect")
public class Collect {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long coinId;
    private String coinName;
    //流水id
    private Long transactionId;
    private String type;
    //合约地址
    private String contractAddress;
    //归集from地址
    private String collectFromAddress;
    //归集to地址
    private String collectToAddress;
    //归集金额
    private BigDecimal collectAmount;
    //归集hash
    private String collectHash;
    //ctime
    private Date ctime;
    //mtime
    private Date mtime;

    private String chainType;

}
