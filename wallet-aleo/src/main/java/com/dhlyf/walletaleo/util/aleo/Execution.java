package com.dhlyf.walletaleo.util.aleo;

import lombok.Data;

import java.util.List;

@Data
public class Execution {

    private List<Transition> transitions;
    private String global_state_root;
    private String proof;
}
