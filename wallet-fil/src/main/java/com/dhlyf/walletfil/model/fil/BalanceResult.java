package com.dhlyf.walletfil.model.fil;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BalanceResult implements Serializable {
    /**
     * 余额
     */
    private BigDecimal balance;

    private String jsonrpc;


    @JsonProperty("result")
    private String result;

    private Integer id;
}

