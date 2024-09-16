    package org.example.payservice.Crypto;

    import org.example.payservice.Entity.Account;
    import org.example.payservice.Entity.Chain;
    import org.example.payservice.Excepion.SendTransactionError;
    import org.example.payservice.Repositories.AccountRepository;
    import org.example.payservice.Repositories.ChainRepository;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.web3j.abi.FunctionEncoder;
    import org.web3j.abi.TypeReference;
    import org.web3j.abi.datatypes.Address;
    import org.web3j.abi.datatypes.Function;
    import org.web3j.abi.datatypes.generated.Uint256;
    import org.web3j.crypto.*;
    import java.io.BufferedWriter;
    import java.io.FileWriter;
    import java.util.Arrays;
    import java.util.Collections;
    import org.web3j.protocol.Web3j;
    import org.web3j.protocol.core.DefaultBlockParameterName;
    import org.web3j.protocol.core.Response;
    import org.web3j.protocol.core.methods.request.Transaction;
    import org.web3j.protocol.core.methods.response.EthCall;
    import org.web3j.protocol.core.methods.response.EthEstimateGas;
    import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
    import org.web3j.protocol.http.HttpService;
    import org.web3j.tx.Transfer;
    import org.web3j.utils.Convert;
    import org.web3j.utils.Numeric;
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.math.BigInteger;
    import java.security.SecureRandom;

    @Service
    public class Web3jService {
        private final org.slf4j.Logger log = LoggerFactory.getLogger(Web3jService.class);
        private final ChainRepository chainRepository;
        private final AccountRepository accountRepository;
        @Value("${db.private_key}")
        private String privateKey;

        public Web3jService(ChainRepository chainRepository, AccountRepository accountRepository) {
            this.chainRepository = chainRepository;
            this.accountRepository = accountRepository;
        }

        public String createNewAccount(){
            try {
                SecureRandom secureRandom = new SecureRandom();
                byte[] privateKeyBytes = new byte[32]; // 256 бит
                secureRandom.nextBytes(privateKeyBytes);
                ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);
                String address = Keys.getAddress(keyPair);
                String privateKey = keyPair.getPrivateKey().toString(16);
                Account account = new Account(STR."0x\{address}", privateKey);
                accountRepository.save(account);
                return account.getAddress();
            } catch (Exception e) {
                log.error(e.getMessage());
                return null;
            }
        }

        public boolean sendMoney(Chain chain,String token, Account account, String recipientAddress, BigDecimal amount) {
            try {
                String privateKey = account.getPrivateKey();
                Web3j web3j = Web3j.build(new HttpService(chain.getRpc()));
                Credentials credentials = Credentials.create(privateKey);
                BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
                BigInteger estimatedGasLimit = BigInteger.valueOf(21000);
                BigInteger totalGasCost = gasPrice.multiply(estimatedGasLimit);
                if (token.equals("native")) sendNativeTransfer(web3j, credentials, recipientAddress, totalGasCost, amount);
                else sendUsdtTransfer(web3j, credentials, chain.getContractUSDT(), account, recipientAddress);
                return true;
            }
            catch (Exception err){
                String log = account.getAddress()+" - "+err.getMessage();
                writeLogError(log);
                return false;
            }
        }

        public boolean sendMoney(Chain chain,String token, Account account, String recipientAddress){
            return sendMoney(chain, token, account, recipientAddress, null);
        }

        public void sendUsdtTransfer(Web3j web3j, Credentials credentials, String contractUSDT, Account account, String recipientAddress) throws IOException, SendTransactionError {
            BigInteger balanceUsdt = getUsdtBalance(web3j, contractUSDT, account.getAddress());
            Response.Error error = web3j.ethSendRawTransaction(getTransaction(web3j, recipientAddress, balanceUsdt, contractUSDT, credentials)).send().getError();
            if (error!=null){
                throw new SendTransactionError(error.getMessage());
            }
        }

        private String getTransaction(Web3j web3j, String recipientAddress, BigInteger balance, String contract, Credentials credentials) throws IOException {
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
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
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    getMaxGazForThisTransaction(data, web3j),
                    contract,
                    data
            );
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            return Numeric.toHexString(signedMessage);
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

        public void sendNativeTransfer(Web3j web3j, Credentials credentials, String recipientAddress, BigInteger gaz, BigDecimal amount) throws Exception {
            if (amount==null) amount = getBalanceNative(web3j, credentials, gaz);
            Transfer.sendFunds(
                    web3j,
                    credentials,
                    recipientAddress,
                    amount,
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

        private void writeLogError(String newLog){
            String filePath = "P:\\payCrS\\payService\\src\\main\\java\\org\\example\\payservice\\Crypto\\log.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.newLine();
                writer.write(newLog);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        private BigInteger getMaxGazForThisTransaction(String encodeFunc, Web3j web3j) throws IOException {
            EthEstimateGas estimateGas = web3j.ethEstimateGas(
                            Transaction.createEthCallTransaction("0xF29466ca1622e16860797C1979C2C3cEA501CEf0", "0xF29466ca1622e16860797C1979C2C3cEA501CEf0", encodeFunc))
                    .send();
            return estimateGas.getAmountUsed().multiply(BigInteger.valueOf(4));
        }

        public BigDecimal getGazForReguralTransaction(){
            return fromGweiToNativeToken(new BigInteger("30000"));
        }

        private BigDecimal fromGweiToNativeToken(BigInteger gwei){
            return Convert.fromWei(gwei.toString(), Convert.Unit.GWEI);
        }

        public BigDecimal getGazForTransactionTokenERC20(String chainId) {
            Chain chain = chainRepository.findByChainId(chainId);
            Web3j web3j = Web3j.build(new HttpService(chain.getRpc()));
            BigInteger randomAmount = new BigInteger("10000000000000000");
            Address randomAddress = new Address("0xF29466ca1622e16860797C1979C2C3cEA501CEf0");
            Function transferFunction = new Function(
                    "transfer",
                    Arrays.asList(randomAddress, new Uint256(randomAmount)),
                    Collections.emptyList()
            );
            String data = FunctionEncoder.encode(transferFunction);
            return getGazInNativeToken(data, web3j);
        }

        private BigDecimal getGazInNativeToken(String encodeFunc, Web3j web3j) {
            try {
                BigInteger gazInGwei = getMaxGazForThisTransaction(encodeFunc, web3j);
                return fromGweiToNativeToken(gazInGwei);
            }
            catch (Exception err){
                log.error(err.getMessage());
                return null;
            }
        }

        public void sendTokenForFee(String chainId, String recipientAddress){
            Chain chain = chainRepository.findByChainId(chainId);
            sendMoney(chain, "native", new Account("bank", privateKey), recipientAddress, getGazForTransactionTokenERC20(chainId));
        }
    }