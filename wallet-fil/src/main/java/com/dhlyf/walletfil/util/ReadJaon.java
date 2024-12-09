package com.dhlyf.walletfil.util;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;

public class ReadJaon {

    //main
    public static void main(String[] args) {
        File file = new File("/Users/dinghui/Downloads/FIL_50000.txt");
        String json = FileUtil.readUtf8String(file);
        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        createTronSql(jsonArray);
        createUserSql(jsonArray);
    }

    private static void createUserSql(JSONArray jsonArray) {
        //status默认 0  address: TRY8PPTEGmtB24riSULaZuWxN9KGpNCJAk  coin_protocol 默认 6 member_id默认 0
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into addressext (status,address,coin_protocol,member_id) values ");
        stringBuffer.append("\n");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            String address = jsonObject1.getString("address");
            stringBuffer.append("(0,'").append(address).append("',6,0),");
            //换行
            stringBuffer.append("\n");
        }
        //去掉最后一个逗号
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.append(";");
        String sql = stringBuffer.toString();
        File file1 = FileUtil.appendString(sql,new File("/Users/dinghui/Downloads/userAddress.sql"),"utf-8");
        //写入磁盘
        System.out.println("createUserSql success path in " + file1.getAbsolutePath());
    }

    public static void createTronSql(JSONArray jsonArray) {
        //遍历jsonArray 获取每个jsonObject 拼接insert语句 insert into address (address,private_key) values ('address','privateKey');
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into address (`address`,`private_key`) values ");
        stringBuffer.append("\n");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            String address = jsonObject1.getString("address");
            String private_key = jsonObject1.getString("privateKey");
            stringBuffer.append("('").append(address).append("','").append(private_key).append("'),");
            //换行
            stringBuffer.append("\n");
        }
        //去掉最后一个逗号
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        stringBuffer.append(";");
        String sql = stringBuffer.toString();
        File file1 = FileUtil.appendString(sql,new File("/Users/dinghui/Downloads/wallet.sql"),"utf-8");
        //写入磁盘
        System.out.println("createTronSql success path in " + file1.getAbsolutePath());
    }
}
