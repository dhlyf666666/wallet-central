package com.dhlyf.walletfil.model.fil;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageItem {

    @JsonProperty("Version")
    private long Version;

    @JsonProperty("To")
    private String To;

    @JsonProperty("From")
    private String From;

    @JsonProperty("Nonce")
    private long Nonce;

    @JsonProperty("Value")
    private String Value;

    @JsonProperty("GasLimit")
    private long GasLimit;

    @JsonProperty("GasFeeCap")
    private String GasFeeCap;

    @JsonProperty("GasPremium")
    private String GasPremium;

    @JsonProperty("Method")
    private long Method;

    @JsonProperty("Params")
    private String Params;

    @JsonProperty("CID")
    private Result.CidItem CID;
}
