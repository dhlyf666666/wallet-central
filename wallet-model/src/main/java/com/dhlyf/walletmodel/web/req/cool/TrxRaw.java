package com.dhlyf.walletmodel.web.req.cool;

import lombok.Data;

import java.util.List;

@Data
public class TrxRaw {

    private boolean visible;
    private String txID;
    private RawData raw_data;
    private String raw_data_hex;
    private String privateKey;

    // Getter和Setter
    // 省略了getter和setter方法以简化代码
    @Data
    public static class RawData {
        private List<Contract> contract;
        private String ref_block_bytes;
        private String ref_block_hash;
        private long expiration;
        private long timestamp;

        // Getter和Setter
        // 省略了getter和setter方法以简化代码
    }

    @Data
    public static class Contract {
        private Parameter parameter;
        private String type;

        // Getter和Setter
        // 省略了getter和setter方法以简化代码
    }

    @Data
    public static class Parameter {
        private Value value;
        private String type_url;

        // Getter和Setter
        // 省略了getter和setter方法以简化代码
    }

    @Data
    public static class Value {
        private long amount;
        private String owner_address;
        private String to_address;

        // Getter和Setter
        // 省略了getter和setter方法以简化代码
    }

    // 根据需要添加toString方法等

}
