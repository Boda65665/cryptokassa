package org.example.payservice.Crypto;

import org.example.payservice.Entity.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConverter {
    private static final PriceCryptoAPI priceCryptoAPI = new PriceCryptoAPI();

    public static Double fromCryptoToUSD(Transaction transaction){
        if (transaction.getToken().equals("native")){
            double price;
            if (transaction.getChain().equals("BSC")){
                price = priceCryptoAPI.getPrice("BNB");
            }
            else price=priceCryptoAPI.getPrice("ETH");
            BigDecimal equivalentInUSD = new BigDecimal(price*transaction.getValue());
            equivalentInUSD = equivalentInUSD.setScale(3, RoundingMode.HALF_UP);
            return equivalentInUSD.doubleValue();
        }
        else {
            return transaction.getValue();
        }
    }
}
