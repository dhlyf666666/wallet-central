package com.dhlyf.walletfil.model.fil;

import com.alibaba.fastjson2.JSONObject;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcPar implements Serializable {
    private static final long serialVersionUID = 487079608019000310L;
    private String jsonrpc;
    private Integer id;
    private String method;
    private List<Object> params;

     public String toJSONString() {
         return JSONObject.toJSONString(this);
     }
}
