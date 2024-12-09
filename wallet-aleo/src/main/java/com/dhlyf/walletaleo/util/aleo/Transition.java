package com.dhlyf.walletaleo.util.aleo;

import lombok.Data;

import java.util.List;

@Data
public class Transition {

    private String id;
    private String program;
    private String function;
    private List<Input> inputs;
    private List<Output> outputs;
    private String tpk;
    private String tcm;
    private String scm;
}
