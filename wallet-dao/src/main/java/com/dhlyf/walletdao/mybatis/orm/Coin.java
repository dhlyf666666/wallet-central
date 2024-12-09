package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("coin")
public class Coin {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    //币种类型
    private String type;

    //合约地址
    private String address;

    //币种名称
    private String coinName;

    //币种精度
    private Integer coinDecimals;

    //币种确认数
    private Integer coinConfirm;

    //最小归集数量
    private BigDecimal minCollectAmount;

    //创建时间
    private Date ctime;

    //更新时间
    private Date mtime;

    private String chainType;

}
