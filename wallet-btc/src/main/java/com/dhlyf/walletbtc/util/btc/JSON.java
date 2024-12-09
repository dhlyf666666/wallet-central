package com.dhlyf.walletbtc.util.btc;

import com.alibaba.fastjson2.JSONObject;

public class JSON {
    public JSON() {
    }

    public static String stringify(Object o) {
        return JSONObject.toJSONString(o);
    }

    public static Object parse(String s) {
        return CrippledJavaScriptParser.parseJSExpr(s);
    }
}
