package com.dhlyf.walletdao.mybatis.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("notify")
public class Notify {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String applicationType;
    private String params;
    private String url;
    //0通知 1成功 2异常 3异常终止
    private Integer status;
    private Integer errorCount;
    private String message;
    private Date ctime;
    private Date mtime;

    private String chainType;

}
