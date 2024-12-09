package com.dhlyf.walletaleo.util.aleo;

import lombok.Data;

import java.util.List;

@Data
public class ProgramArguments {
    private String program_id;
    private String function_name;
    private List<String> arguments;
}
