package com.dhlyf.walletfil.util;

import cn.hutool.core.codec.Base32;
import cn.hutool.core.util.StrUtil;
import com.dhlyf.walletfil.model.fil.Address;

public class AddressUtil {

  /**
   * secp256k1地址字节长度
   */
  private final static int addressLength = 21;

  public static Address initAddress(String addressStr) {
    if (StrUtil.isBlank(addressStr)) {
      throw new NullPointerException("addressStr 参数不能为空");
    }
    //去掉拼接的前两位
    String substring = addressStr.substring(2);
    //获取type
    String typeStr = addressStr.substring(1, 2);
    //获取网络类型
    String network = addressStr.substring(0, 1);
    int type = Integer.parseInt(typeStr);

    byte[] addressBytes = new byte[addressLength];

    switch (type) {
      case 1:
        //secp256k1 地址类型
        addressBytes[0] = (byte) type;
        break;
    }

    System.arraycopy(Base32.decode(substring), 0, addressBytes, 1, 20);
    return Address.builder().address(addressStr)
        .bytes(addressBytes)
        .network(network).build();

  }
}
