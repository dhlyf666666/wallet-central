
package com.dhlyf.walletmodel.web.req;

import com.dhlyf.walletmodel.common.Chain;
import lombok.Data;

@Data
public class RequestData<T> {

    private String dataString;
    private Chain chain;
    private T data;

}
