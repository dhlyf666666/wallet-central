package com.dhlyf.walletaleo.util.aleo;

import lombok.Data;

@Data
public class Fee {

    private Transition transition;
    private String global_state_root;
    private String proof;
}
