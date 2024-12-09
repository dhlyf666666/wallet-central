package com.dhlyf.walletaleo.util.aleo;

import lombok.Data;

import java.util.List;

@Data
public class BlockTransactions {

    private String status;
    private String type;
    private int index;
    private Transaction transaction;
    private List<FinalizeUpdate> finalize;

}
