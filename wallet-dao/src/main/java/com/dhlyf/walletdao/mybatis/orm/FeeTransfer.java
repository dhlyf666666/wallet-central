package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("fee_transfer")
public class FeeTransfer {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long accountId;
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private String txid;
    private Integer status;
    private Date ctime;
    private Date mtime;

    private String chainType;

}
