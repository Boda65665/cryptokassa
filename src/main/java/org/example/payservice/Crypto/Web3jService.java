    package org.example.payservice.Crypto;

    import org.example.payservice.Entity.Account;
    import org.example.payservice.Entity.Chain;
    import org.example.payservice.Repositories.AccountRepository;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Service;
    import org.web3j.abi.FunctionEncoder;
    import org.web3j.abi.TypeReference;
    import org.web3j.abi.datatypes.Address;
    import org.web3j.abi.datatypes.Function;
    import org.web3j.abi.datatypes.generated.Uint256;
    import org.web3j.crypto.Credentials;
    import org.web3j.crypto.ECKeyPair;
    import org.web3j.crypto.Keys;
    import java.util.Arrays;
    import java.util.Collections;
    import org.web3j.protocol.Web3j;
    import org.web3j.protocol.core.DefaultBlockParameterName;
    import org.web3j.protocol.core.methods.request.Transaction;
    import org.web3j.protocol.core.methods.response.EthCall;
    import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
    import org.web3j.protocol.http.HttpService;
    import org.web3j.tx.Transfer;
    import org.web3j.utils.Convert;
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.math.BigInteger;
    import java.security.SecureRandom;
    import java.util.logging.Logger;

    @Service
    public class Web3jService {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger(Web3jService.class);
        private final AccountRepository accountRepository;

        public Web3jService(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public Account createNewAccount(){
            try {
                SecureRandom secureRandom = new SecureRandom();
                byte[] privateKeyBytes = new byte[32]; // 256 бит
                secureRandom.nextBytes(privateKeyBytes);
                ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);
                String address = Keys.getAddress(keyPair);
                String privateKey = keyPair.getPrivateKey().toString(16); // Приватный ключ в шестнадцатеричном формате
                Account newAccount = new Account(address, privateKey);
                accountRepository.save(newAccount);
                return newAccount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public boolean sendMoney(Chain chain,String token, Account account, String recipientAddress) {
            try {
                String privateKey = account.getPrivateKey();
                Web3j web3j = Web3j.build(new HttpService(chain.getRpc()));
                Credentials credentials = Credentials.create(privateKey);
                BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
                BigInteger estimatedGasLimit = BigInteger.valueOf(21000); 
                BigInteger totalGasCost = gasPrice.multiply(estimatedGasLimit);
                if (token.equals("native")) sendNativeTransfer(web3j, credentials, recipientAddress, totalGasCost);
                else sendUsdtTransfer(web3j, credentials, chain.getContractUSDT(), account, recipientAddress);
            }
            catch (Exception err){
                log.error("d");
            }
            return false;
        }

        public void sendUsdtTransfer(Web3j web3j, Credentials credentials, String contractUSDT, Account account, String recipientAddress) throws IOException {
            BigInteger balanceUsdt = getUsdtBalance(web3j, contractUSDT, account.getAddress());
            String transactionHash = web3j.ethSendTransaction(getTransaction(web3j, recipientAddress, balanceUsdt, contractUSDT, credentials)).send().getTransactionHash();
            System.out.println(transactionHash);
        }

        private static Transaction getTransaction(Web3j web3j, String recipientAddress, BigInteger balance, String contract, Credentials credentials) throws IOException {
            BigInteger gasLimit = BigInteger.valueOf(21000); // Установите лимит газа
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice(); // Получите цену газа
            EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(),
                    DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = transactionCount.getTransactionCount();
            Function transferFunction = new Function(
                    "transfer",
                    Arrays.asList(new Address(recipientAddress), new Uint256(balance)),
                    Collections.emptyList()
            );

            String data = FunctionEncoder.encode(transferFunction);
            return Transaction.createFunctionCallTransaction(
                    credentials.getAddress(),
                    nonce,
                    gasPrice,
                    gasLimit,
                    contract,
                    data
            );
        }
        public BigInteger getUsdtBalance(Web3j web3j, String contract, String address) throws IOException {
            Function balanceOfFunction = new Function(
                    "balanceOf",
                    Collections.singletonList(new Address(address)),
                    Collections.singletonList(new TypeReference<Uint256>() {})
            );
            String encodedFunction = FunctionEncoder.encode(balanceOfFunction);

            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(address, contract, encodedFunction),
                    DefaultBlockParameterName.LATEST
            ).send();

            return new BigInteger(response.getValue().substring(2), 16);
        }


        public void sendNativeTransfer(Web3j web3j, Credentials credentials, String recipientAddress, BigInteger gaz) throws Exception {
            Transfer.sendFunds(
                    web3j,
                    credentials,
                    recipientAddress,
                    getBalanceNative(web3j, credentials, gaz),
                    Convert.Unit.ETHER
            ).send();
        }

        public BigDecimal getBalanceNative(Web3j web3j, Credentials credentials, BigInteger gaz) throws IOException {
                BigInteger balance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                        .send()
                        .getBalance();
                BigInteger amount = balance.subtract(gaz);
                return Convert.fromWei(new BigDecimal(amount), Convert.Unit.ETHER);
        }
    }