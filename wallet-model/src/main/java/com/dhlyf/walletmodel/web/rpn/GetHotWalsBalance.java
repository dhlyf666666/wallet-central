package com.dhlyf.walletmodel.web.rpn;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetHotWalsBalance {
    private String contractAddress;
    private String address;
    private BigDecimal balance;
}
