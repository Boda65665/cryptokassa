package org.example.payservice;

import org.example.payservice.Crypto.Web3jService;
import org.example.payservice.Entity.Account;
import org.example.payservice.Entity.Chain;
import org.example.payservice.Repositories.ChainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PayServiceApplication.class)
public class Web3ServiceTest {
    @Autowired
    private Web3jService web3jService;
    @Autowired
    private ChainRepository chainRepository;

    @Test
    public void sendTransactionTest(){
        Account account = new Account("0xf29466ca1622e16860797c1979c2c3cea501cef0", "0x247cbda5ad6e1d02fd26e3b0ac326f8edb03942293a6da8c9b3e3b886d87aa42");
        web3jService.sendMoney(chainRepository.findByChainId("BSC"), "USDT", account, "0xf29466ca1622e16860797c1979c2c3cea501cef0");
    }
}
