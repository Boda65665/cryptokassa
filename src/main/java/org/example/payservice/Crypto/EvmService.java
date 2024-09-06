package org.example.payservice.Crypto;

import org.example.payservice.Entity.Chain;
import org.example.payservice.Entity.Invoice;
import org.example.payservice.Entity.Transaction;
import org.example.payservice.Repositories.ChainRepository;
import org.example.payservice.Repositories.InvoiceRepository;
import org.example.payservice.Repositories.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.http.HttpService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EvmService {
    private final Logger logger = Logger.getLogger("payService");
    private final ChainRepository chainRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;

    public EvmService(ChainRepository chainRepository, TransactionRepository transactionRepository, InvoiceRepository invoiceRepository) {
        this.chainRepository = chainRepository;
        this.transactionRepository = transactionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Scheduled(fixedRate = 3000)
    private void subscribeForNewBlock() {
        List<Chain> chains = chainRepository.findAllByType("evm");
        for (Chain chain : chains) {
            Web3j web3 = Web3j.build(new HttpService(chain.getRpc()));
            try {
                BigInteger lastBlock = chain.getLastBlockOrTransaction();
                EthBlock ethBlock;
                if (lastBlock == null) {
                    ethBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send();
                } else {
                    ethBlock = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(lastBlock), true).send();
                }
                EthBlock.Block block = ethBlock.getBlock();
                if (block == null) continue;
                sendTransactionToSubscribeServer(block.getTransactions(), chain);
                if (lastBlock == null) chainRepository.updateLastBlockOrTransaction(block.getNumber(), chain.getChainId());
                else chainRepository.updateLastBlockOrTransaction(lastBlock.add(BigInteger.ONE), chain.getChainId());
            } catch (Exception err) {
                logger.log(Level.WARNING, err.toString());
            }
        }
    }

    private void sendTransactionToSubscribeServer(List<TransactionResult> transactionsResult, Chain chain) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (TransactionResult transactionRes : transactionsResult) {
            EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionRes.get();
            org.web3j.protocol.core.methods.response.Transaction transaction = transactionObject.get();
            if (transaction.getTo() != null) {
                List<Invoice> invoices = invoiceRepository.findAll();
                for (Invoice invoice :invoices) {
                    String address = invoice.getAddress();
                    if (transaction.getTo().equalsIgnoreCase(address)) {
                        Double value = new BigDecimal(transaction.getValue()).divide(new BigDecimal(BigInteger.TEN.pow(18))).doubleValue();
                        transactions.add(new Transaction(transaction.getTo(), transaction.getFrom(), "native", value, chain.getChainId(), transaction.getHash() ,invoice.getIdClient()));
                        invoiceRepository.delete(invoice);
                        System.out.println("Мы получили транзакцию от "+address+", и ждем " + chain.getAwaitingConfirmation() + " подтверждений");
                    } else if (chain.getChainId().equals("BSC") && transaction.getTo().equalsIgnoreCase(chain.getContractUSDT())) {
                        String inputData = transaction.getInput();
                        if (inputData.startsWith("0xa9059cbb")) { // Метод transfer(address,uint256)
                            String toAddress = "0x" + inputData.substring(34, 74);
                            if (toAddress.equalsIgnoreCase(address)) {
                                String valueHex = inputData.substring(74);
                                BigInteger value = new BigInteger(valueHex, 16);
                                Double usdtValue = new BigDecimal(value).divide(chain.getDivideByUsdtForConvert(), 2, RoundingMode.HALF_UP).doubleValue();
                                transactions.add(new Transaction(toAddress, transaction.getFrom(),"usdt", usdtValue, chain.getChainId(), transaction.getHash(), invoice.getIdClient()));
                                invoiceRepository.delete(invoice);
                                System.out.println("Мы получили вашу транзакцию, и ждем " + chain.getAwaitingConfirmation() + " подтверждений");
                            }
                        }
                    }
                }
            }
        }
        if (!transactions.isEmpty()) transactionRepository.saveAll(transactions);
    }

    @Scheduled(fixedRate = 2000)
    private void findConfirmedTransactionInPoolPendingTransaction(){
        List<Transaction> transactions = transactionRepository.findAll();
        for (Transaction transaction : transactions) {
            Chain chain = chainRepository.findByChainId(transaction.getChain());
            Web3j web3j = Web3j.build(new HttpService(chain.getRpc()));
            try {
                EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transaction.getHash()).send();
                if (transactionReceipt.getTransactionReceipt().get().getStatus().equals("0x1")){
                    EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
                    BigInteger awaitBlock = transactionReceipt.getTransactionReceipt().get().getBlockNumber().add(new BigInteger(String.valueOf(chain.getAwaitingConfirmation())));
                    if (awaitBlock.compareTo(blockNumber.getBlockNumber())<0){
                        transactionRepository.delete(transaction);

                    }
                }
                else transactionRepository.delete(transaction);
            }
            catch (Exception err){
                logger.log(Level.WARNING, err.toString());
            }
        }
    }
}

