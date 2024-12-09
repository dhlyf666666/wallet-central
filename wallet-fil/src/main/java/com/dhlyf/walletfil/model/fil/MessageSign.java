package com.dhlyf.walletfil.model.fil;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageSign {

    @JsonProperty("Type")
    private int Type;

    @JsonProperty("Data")
    private String Data;
}
