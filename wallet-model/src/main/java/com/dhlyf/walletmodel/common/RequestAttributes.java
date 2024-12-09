package com.dhlyf.walletmodel.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestAttributes {

    private Language language;
    private CacheUser user;
}
