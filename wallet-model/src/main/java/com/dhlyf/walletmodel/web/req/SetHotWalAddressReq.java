package com.dhlyf.walletmodel.web.req;

import lombok.Data;

@Data
public class SetHotWalAddressReq {
    private String address;
    private Long uid;
}
