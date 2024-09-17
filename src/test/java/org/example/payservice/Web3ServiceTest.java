package org.example.payservice;

import org.example.payservice.Crypto.Web3jService;
import org.example.payservice.Entity.Account;
import org.example.payservice.Entity.Chain;
import org.example.payservice.Repositories.ChainRepository;
import org.example.payservice.Sequrity.Encrypted;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@SpringBootTest(classes = PayServiceApplication.class)
public class Web3ServiceTest {
    @Autowired
    private Web3jService web3jService;
    @Autowired
    private ChainRepository chainRepository;
    @Autowired
    private Encrypted encrypted;

    @Test
    public void createNeAccount(){
        Account account = web3jService.createNewAccount();
        Assertions.assertNotNull(account);

        String address = account.getAddress();
        Assertions.assertNotNull(address);
        Assertions.assertTrue(address.startsWith("0x"));
        Assertions.assertEquals(42, address.length());


        Assertions.assertNotNull(account.getPrivateKey());
        BigInteger privateKey = new BigInteger(account.getPrivateKey(), 16);
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        String recoveredAddress = Keys.getAddress(keyPair);
        Assertions.assertEquals(address, STR."0x\{recoveredAddress}");
    }

    @Test
    public void sendTransactionTest(){
        Account account = new Account("0xf29466ca1622e16860797c1979c2c3cea501cef0", "0x247cbda5ad6e1d02fd26e3b0ac326f8edb03942293a6da8c9b3e3b886d87aa42");
        web3jService.sendMoney(chainRepository.findByChainId("BSC"), "USDT", account, "0x74a4fcee75b6cdf78fef286e153618cdb9f437d6");
    }

    @Test
    public void getTransactionPrice() throws IOException {
        System.out.println(web3jService.getGazForTransactionTokenERC20("BSC"));
    }

    @Test
    public void d() throws Exception {
        String d = encrypted.encrypt("d");
        System.out.println(d);
        System.out.println(encrypted.decrypt(d));
    }
}
