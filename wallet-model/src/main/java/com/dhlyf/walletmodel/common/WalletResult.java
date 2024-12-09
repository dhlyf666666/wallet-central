package com.dhlyf.walletmodel.common;

public enum WalletResult {

    // 成功
    SUCCESS(0, "Success"),
    // 未知错误
    UNKNOWN_ERROR(1, "Unknown error"),
    // 参数错误
    PARAM_ERROR(2, "Parameter error"),
    // 数据库错误
    DB_ERROR(3, "Database error"),
    // 重复操作
    REPEAT_OPERATION(4, "Repeat operation"),
    // 余额不足
    INSUFFICIENT_BALANCE(5, "Insufficient balance"),
    // 无效地址
    INVALID_ADDRESS(6, "Invalid address"),
    // 无效合约地址
    INVALID_CONTRACT_ADDRESS(7, "Invalid contract address"),
    // 无效交易哈希
    INVALID_TX_HASH(8, "Invalid transaction hash"),
    // 无效交易
    INVALID_TRANSACTION(9, "Invalid transaction"),
    // 无效交易类型
    INVALID_TX_TYPE(10, "Invalid transaction type"),
    // 无效交易状态
    INVALID_TX_STATUS(11, "Invalid transaction status"),
    // 无效交易金额
    INVALID_TX_AMOUNT(12, "Invalid transaction amount"),
    // 无效交易手续费
    INVALID_TX_FEE(13, "Invalid transaction fee"),
    // 无效交易时间
    INVALID_TX_TIME(14, "Invalid transaction time"),
    // 无效交易备注
    INVALID_TX_MEMO(15, "Invalid transaction memo"),
    // 无效交易签名
    INVALID_TX_SIGN(16, "Invalid transaction signature"),
    // 无效交易数据
    INVALID_TX_DATA(17, "Invalid transaction data"),
    // 无效交易输入
    INVALID_TX_INPUT(18, "Invalid transaction input"),
    // 无效交易输出
    INVALID_TX_OUTPUT(19, "Invalid transaction output"),
    // 无效交易输入金额
    INVALID_TX_INPUT_AMOUNT(20, "Invalid transaction input amount"),
    // 无效交易输出金额
    INVALID_TX_OUTPUT_AMOUNT(21, "Invalid transaction output amount"),
    // 无效交易输入地址
    INVALID_TX_INPUT_ADDRESS(22, "Invalid transaction input address"),
    // 无效交易输出地址
    INVALID_TX_OUTPUT_ADDRESS(23, "Invalid transaction output address"),
    //商户不存在
    MERCHANT_NOT_EXIST(24, "Merchant does not exist"),
    //签名错误
    SIGN_ERROR(25, "Signature error");

    private int code;
    private String message;

    WalletResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
