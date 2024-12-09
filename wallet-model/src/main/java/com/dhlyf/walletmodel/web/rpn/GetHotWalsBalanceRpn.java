package com.dhlyf.walletmodel.web.rpn;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GetHotWalsBalanceRpn {
    private BigDecimal mainCoinBalance;
    private String mainCoinAddress;
    private List<GetHotWalsBalance> contract;
}
