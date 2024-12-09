package com.dhlyf.walletfil.util;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

public class SmartContractUtil {

    // 计算方法签名的Keccak-256哈希
    public static String getMethodSignatureHash(String methodName) {
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        byte[] hash = digest256.digest(methodName.getBytes());
        return Hex.toHexString(hash).substring(0, 8); // 取前4字节（8个字符）
    }

    // 检查data字段是否表示transfer操作
    public static boolean isTransferOperation(String data) {
        // 这里是"transfer(address,uint256)"方法签名的哈希的前4字节
        // 注意：这个值应该根据你的实际合约方法签名进行预计算或动态计算
        String transferMethodHash = getMethodSignatureHash("transfer(address,uint256)");
        System.out.printf("transferMethodHash: %s\n", transferMethodHash);
        // 检查data的前8个字符是否与transfer方法签名的哈希匹配
        return data.startsWith("a9059cbb");
    }

    public static void main(String[] args) {
        // 示例：检查data是否代表transfer操作
        String data = "a9059cbb000000000000000000000000796f1d781f83d3583e645bcbc62f2469edc8997c00000000000000000000000000000000000000000000000000000000000f4240";
        if (isTransferOperation(data)) {
            System.out.println("是transfer操作");
        } else {
            System.out.println("不是transfer操作");
        }
    }
}

