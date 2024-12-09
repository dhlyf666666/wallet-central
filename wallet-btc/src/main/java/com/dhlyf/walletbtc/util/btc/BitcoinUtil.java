package com.dhlyf.walletbtc.util.btc;


import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class BitcoinUtil {


    public static double normalizeAmount(double amount) {
        return (double)((long)(0.5D + amount / 1.0E-8D)) * 1.0E-8D;
    }

    public static String sendTransaction(Bitcoin bitcoin, String targetAddress, BigDecimal amount, BigDecimal txFee) throws BitcoinException {
        List<Bitcoin.Unspent> unspents = bitcoin.listUnspent(2);
        System.out.println("target=" + targetAddress + ",amount=" + amount.toPlainString() + ",+fee=" + txFee.toPlainString());
        BigDecimal moneySpent = BigDecimal.ZERO;
        BigDecimal moneyChange = BigDecimal.ZERO;
        if (unspents.size() == 0) {
            throw new BitcoinException("insufficient coin");
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String account = "acct-change-" + df.format(new Date());
            String changeAddress = bitcoin.getAccountAddress(account);
            if (changeAddress == null) {
                changeAddress = bitcoin.getNewAddress(account);
            }

            System.out.println("change address:" + changeAddress);
            BitcoinRawTxBuilder builder = new BitcoinRawTxBuilder(bitcoin);
            Iterator var11 = unspents.iterator();

            while(var11.hasNext()) {
                Bitcoin.Unspent unspent = (Bitcoin.Unspent)var11.next();
                moneySpent = moneySpent.add(unspent.amount());
                System.out.println("unspent=" + unspent.amount());
                builder.in(new Bitcoin.BasicTxInput(unspent.txid(), unspent.vout()));
                if (moneySpent.compareTo(amount.add(txFee)) >= 0) {
                    break;
                }
            }

            if (moneySpent.compareTo(amount.add(txFee)) < 0) {
                throw new BitcoinException("insufficient coin");
            } else {
                moneyChange = moneySpent.subtract(amount.add(txFee));
                System.out.println("moneyChange:" + moneyChange.toPlainString());
                builder.out(targetAddress, amount);
                if (moneyChange.compareTo(BigDecimal.ZERO) > 0) {
                    builder.out(changeAddress, moneyChange);
                }

                return builder.send();
            }
        }
    }

    public static String sendTransaction(Bitcoin bitcoin, String fromAddress, String targetAddress, BigDecimal amount, BigDecimal txFee) throws BitcoinException {
        List<Bitcoin.Unspent> unspents = bitcoin.listUnspent(1, 99999999, new String[]{fromAddress});
        System.out.println("target=" + targetAddress + ",amount=" + amount.toPlainString() + ",fee=" + txFee.toPlainString());
        BigDecimal moneySpent = BigDecimal.ZERO;
        BigDecimal moneyChange = BigDecimal.ZERO;
        if (unspents.size() == 0) {
            throw new BitcoinException("insufficient coin");
        } else {
            System.out.println("change address:" + fromAddress);
            BitcoinRawTxBuilder builder = new BitcoinRawTxBuilder(bitcoin);
            Iterator var10 = unspents.iterator();

            while(var10.hasNext()) {
                Bitcoin.Unspent unspent = (Bitcoin.Unspent)var10.next();
                moneySpent = moneySpent.add(unspent.amount());
                System.out.println("unspent=" + unspent.amount());
                builder.in(new Bitcoin.BasicTxInput(unspent.txid(), unspent.vout()));
                if (moneySpent.compareTo(amount.add(txFee)) >= 0) {
                    break;
                }
            }

            if (moneySpent.compareTo(amount.add(txFee)) < 0) {
                throw new BitcoinException("insufficient coin");
            } else {
                moneyChange = moneySpent.subtract(amount.add(txFee));
                System.out.println("moneyChange:" + moneyChange.toPlainString());
                builder.out(targetAddress, amount);
                if (moneyChange.compareTo(BigDecimal.ZERO) > 0) {
                    builder.out(fromAddress, moneyChange);
                }

                return builder.send();
            }
        }
    }
}
