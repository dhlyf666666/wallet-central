package com.dhlyf.walletfil.model.fil;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionsFil implements Serializable {
    private static final long serialVersionUID = -7598602793235403648L;

    private int id;
    @JsonProperty("result")
    private Result result;
    private String jsonrpc;


}
