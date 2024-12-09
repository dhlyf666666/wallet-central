package com.dhlyf.walletsupport.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {

    /**
     * isNonNegative 方法返回的是一个布尔值，用于判断传入的 BigDecimal 数值是否大于或等于0。
     * 	•	如果传入的 BigDecimal 数值为 null 或小于0，isNonNegative 方法返回 false。
     * 	•	如果传入的 BigDecimal 数值大于或等于0，isNonNegative 方法返回 true。
     * @param amount
     * @return
     */
    public static boolean isNonNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }
}
