package com.dhlyf.walletfil.model.fil;

public class FilecoinCnt {

  /**
   *
   */
  public static final int MajUnsignedInt = 0;
  /**
   *
   */
  public static final int MajNegativeInt = 1;
  /**
   *
   */
  public static final int MajByteString = 2;
  /**
   *
   */
  public static final int MajTextString = 3;
  /**
   *
   */
  public static final int MajArray = 4;
  /**
   *
   */
  public static final int MajMap = 5;
  /**
   *
   */
  public static final int MajTag = 6;
  /**
   *
   */
  public static final int MajOther = 7;

  /**
   * 获取gas
   */
  public static final String GET_GAS = "Filecoin.GasEstimateMessageGas";
  /**
   * 获取 nonce值
   */
  public static final String GET_NONCE = "Filecoin.MpoolGetNonce";
  /**
   * 获取 余额
   */
  public static final String GET_BALANCE = "Filecoin.WalletBalance";
  /**
   * 获取 区块交易信息
   */
  public static final String BOARD_TRANSACTION = "Filecoin.MpoolPush";
  /**
   * 校验签名
   */
  public static final String WALLET_VERIFY = "Filecoin.WalletVerify";
  /**
   * 获取一个新的钱包地址
   */
  public static final String NEW_WALLET_ADDRESS = "Filecoin.WalletNew";
  /**
   * 获取Cid
   */
  public static final String ChainGetTipSetByHeight = "Filecoin.ChainGetTipSetByHeight";
  /**
   * 获取交易信息
   */
  public static final String ChainGetBlockMessages = "Filecoin.ChainGetBlockMessages";
  /**
   * 获取区块高度
   */
  public static final String ChainHead = "Filecoin.ChainHead";
  /**
   * 默认超时时间
   */
  public static final int DEFAULT_TIMEOUT = 10000;

}
