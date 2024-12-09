package com.dhlyf.walletfil.model.fil;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SendResult implements Serializable {
    /**
     * 交易CID
     */
    private String cid;

    /**
     * nonce值
     */
    private Long nonce;
}
