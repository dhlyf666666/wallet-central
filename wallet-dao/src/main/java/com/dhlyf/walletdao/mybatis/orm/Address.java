package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("address")
public class Address {

    //id
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    //地址
    private String address;
    //私钥
    private String privateKey;
    //创建时间
    private Date ctime;
    //更新时间
    private Date mtime;

    private String chainType;

}
