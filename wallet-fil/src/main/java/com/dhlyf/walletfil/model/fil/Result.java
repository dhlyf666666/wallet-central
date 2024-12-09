package com.dhlyf.walletfil.model.fil;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Result {

    @JsonProperty("Cids")
    private List<CidItem> Cids;

    @JsonProperty("SecpkMessages")
    private List<SecpkMessagesItem> SecpkMessages;

    @JsonProperty("Blocks")
    private List<Object> Blocks;

    @JsonProperty("Height")
    private long Height;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class SecpkMessagesItem{
        @JsonProperty("Message")
        private MessageItem Message;
        @JsonProperty("Signature")
        private MessageSign Signature;
        @JsonProperty("CID")
        private CidItem CID;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class CidItem {
        @JsonProperty("/")
        private String cid;
    }

}
