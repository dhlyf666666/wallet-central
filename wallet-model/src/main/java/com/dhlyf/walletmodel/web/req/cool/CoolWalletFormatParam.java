package com.dhlyf.walletmodel.web.req.cool;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.web.req.RequestData;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class CoolWalletFormatParam {

    //main
    public static void main(String[] args) {

        JSONObject req = new JSONObject();
        req.put("lastId", 1L);
        RequestData requestData = formatParma(req, Chain.TRX);
        System.out.printf("requestData: %s\n", requestData.getDataString());
    }


    public static RequestData formatParma(Object paramObject, Chain chain) {
        Map<String,Object> paramMap = sign(paramObject);
        String paramJson = JSONObject.toJSONString(paramMap);
        //先用自己私钥加密
        String d = prikeyDecryptParam(paramJson);
        //再用finance的公钥加密
        String k = publicEncryptBase64(d);
        RequestData requestData = new RequestData();
        requestData.setDataString(k);
        requestData.setData(null);
        requestData.setChain(chain);
        return requestData;
    }

    /**
     * 用自己的私钥加密
     * @param str
     * @return
     */
    private static String prikeyDecryptParam(String str) {
//        String m = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCC9sFV9FPxMRtKJ2ACRXSXTp5GnIIQF+C2V2NT2AoFZx4mSddBA2wzuPRiwMraGimr74Pzb6cN83tAqdUCUyoZ9iGquJlWyZxVq3rPSteDPtan7lzizcmsBws2BjgniA513eavEi4iSO6Z+idWRQz/JqhDIGJ44kobaOSjJI0Xpa1Dgwrc1pAyPWCsC8rhc/dpVOsFRH957pK5mUKzl+io+sJtDxLk/Jc8xk8UXz7HvdohrLBKpx9ALehIkfSs1tpRLsodUZm3NMFEk9LJIcM4b3b3oTJ0IRgkSyMTc3eDG2ygSqSJH+aOcZFsj5Use6p6r/pda6Xtx8DlPw/d9Bt3AgMBAAECggEAKp7DLIXnC4c/qlJGHHceCl+7gp9MdJBQwQaPfKnCGSAF39AIDVBf1jVK0xiK64+4IAxClOaxJwkmA1VBzQ/jk69WJYkN3gCesAKuZOemW0AH+37HG8h86U8hhuNfQv40NoeQm0j/WokBnXqbcqfmIuMhvza61TxPgbwRLEao5Bi7qVQA2MBwO8a3ob8S2QJi+mEyMU8JpB4L0gXeJP5QJ15facTKFWRMJyULbkQ2QWMJfJAB+XJIpOQRBwFGGwRc7bS7OC3MtAOIQoACVW+Nba1CxymIx0qTnZ9OtUy2A1NmbJJ8jEZQ98304iESSd+VUVD5mx8+SIKze0TI68HHgQKBgQDtL7ObbTa6afTq54fR5JMnavluI3GhbLgvGVfn3sXqe9BZUWGodJTXpe/HlYai3wWVVFEK9KaJBjNXOeX7zro957eFW/GHP8iGJgQbx9OK03bs4UL0rr0QZORnYAYmNlAd4bgv9LECYXGsR6JkezyOLGExRTPrwkqZ/ubYm2d+rQKBgQCNWhqYksDzmGExfS9qlU0s+8+GpYF/6QPhm9FjiQD7nJI6JpLd7gYrYgKrXmTxHHYS399oiQk0MiTwLiPYQ5Rag8GtExRYu/sozWr5xb9+hnNx1hOJC0GF0yWsSCCN/JjfvbJq/gZzRiu4x7Y2IWFOCRhOCeZgxkSLv9X0/zQ7MwKBgBPpk0yTKKsRxiwKcA6g//GpO3ORqjKBLB1YJVckDr2W03EMMgSwdOZ92dmJphn6Wz+kKoGe9HRAfwLtq28uddhIodGiXG6cXjtU6bdjWVEoW62bY8GjAeBkeABtwVZn7OX1Uz9DbbDjeWWbRfjll7nf4/D8/FTopNa03bCFq3SpAoGATKdUTPEomfFEo+XTVOVqWL6lp8sREyB17l7liii907Twx4ArnRZ7WgNoly41Z1ub3Fhzeuj1iMy4o1ciMSFUzlEtMoJqQkSOy226WYEXe7HF4THKnkLwYXZDs83ZeaZF8O+aVdM0BEdFZotHEMvBTOQ5vfg4arqaaX66tIXA+WkCgYEA6QR3N69Wa7wxVxeP2RVp+82u4dJ49nmZGCde0YFqcjKKkRcw39wu3ZFz3W73SaeXkO9Epgv/H1JhP3Ac4ijJgAagzGwFOKJvnMT4F9YgfJMHedpvooGasFLPNtslRtIGGaGYysUR8zu3Axy2PO//Id1DrVUZDt77dPiQ/ViNFws=";
        String m = SpringContextUtil.getProperty("REQUEST_PRIVATE_KEY");
//        log.info("prikeyDecryptParam m:{}",m);
        RSA rsa = new RSA(m, null);
        return rsa.encryptBase64(str, KeyType.PrivateKey);
    }

    /**
     * 用finance的公钥加密
     * @param str
     * @return
     */
    private static String publicEncryptBase64(String str) {
//        String m = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyIYCJAKsRrr2zmSuT/EqLpjQBmgXL9f29QpsWWa2+DJLQ2G9rk/5wW3rOI9FBm5ymzIG0oYI9sZsAhWnPf661S3T10KivKh7501MUt34Yzi7evKH3de1iq+J95tnlUqARHzXlBTpuOHfx3mw1AHlg7DybSp3B9y1egapFW8eTnMcE6R4YDCvWc/s5ehTVXB+0jbwwGkPba9LlwldXq4h2Tnb99uLdU+aAL0HzvIYxV/Rk2yqJ7F/cm5P4amF/+OzrqpH2RYbSIp1kdtZcxUNZT4rY/sGw6hb3BcChMPdjPoYb6tGSbKxiY6rXzA14pG638Zdw+H2LKLT5FHhc3qcgwIDAQAB";
        String m = SpringContextUtil.getProperty("COOL_PUBLIC_KEY");
//        log.info("publicEncryptBase64 m:{}",m);
        RSA rsa = new RSA(null, m);
        return rsa.encryptBase64(str, KeyType.PublicKey);
    }

    public static Map<String,Object> sign(Object object) {
        String m = SpringContextUtil.getProperty("REQUEST_MD5_SIGN_KEY");
//        log.info("sign m:{}",m);
        TreeMap<String, String> treeMap = JSONObject.parseObject(JSONObject.toJSONString(object),
                new TypeReference<TreeMap<String, String>>() {
                });
        StringBuilder sb = new StringBuilder();
        for (String key : treeMap.keySet()) {
            //空value key=sing跳过
            if (treeMap.get(key) == null || "".equals(treeMap.get(key)) || "sign".equals(key)) {
                continue;
            }
            sb.append(key).append("=").append(treeMap.get(key)).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        String paramSign = BCrypt.hashpw(sb.toString() + m);
        Map<String,Object> rtn = new TreeMap<>();
        rtn.putAll(treeMap);
        rtn.put("sign",paramSign);
        return rtn;
    }


}
