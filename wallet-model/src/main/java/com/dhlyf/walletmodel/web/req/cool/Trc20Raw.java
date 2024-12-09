package com.dhlyf.walletmodel.web.req.cool;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Trc20Raw {

    private String txID;
    private boolean visible;
    private RawData raw_data;
    private String raw_data_hex;
    private String address;
    private String privateKey;

    @Data
    @Builder
    public static class RawData {
        private List<Contract> contract;
        private long fee_limit;
        private long timestamp;
        private long expiration;
        private String ref_block_hash;
        private String ref_block_bytes;

        // Getters and Setters
    }

    @Data
    @Builder
    public static class Contract {
        private String type;
        private Parameter parameter;

    }

    @Data
    @Builder
    public static class Parameter {
        private Value value;
        private String type_url;

    }

    @Data
    @Builder
    public static class Value {
        private String data;
        private String owner_address;
        private String contract_address;

    }
}
