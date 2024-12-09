package com.dhlyf.walletaleo.util.aleo;

import lombok.Data;

@Data
public class Transaction {

    private String type;
    private String id;
    private Execution execution;
    private Fee fee;
}
