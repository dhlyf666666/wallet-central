package com.dhlyf.walletmodel.web.req;

import lombok.Data;

@Data
public class ParamRequest<T extends IRequestParams> {
    private String method;
    private String chain;
    private T params;
}
