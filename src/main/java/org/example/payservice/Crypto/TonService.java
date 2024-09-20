package org.example.payservice.Crypto;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.payservice.Entity.Chain;
import org.example.payservice.HttpRequest.OkHttp;
import org.example.payservice.HttpRequest.RequestBody;
import org.example.payservice.Repositories.ChainRepository;
import org.example.payservice.Service.JwtService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

@Component
public class TonService {
    private final OkHttp okHttp = new OkHttp();
    private final ChainRepository chainRepository;
    private final JwtService jwtService;
    private final PriceCryptoAPI priceCryptoAPI = new PriceCryptoAPI();

    public TonService(ChainRepository chainRepository, JwtService jwtService) {
        this.chainRepository = chainRepository;
        this.jwtService = jwtService;
    }

    @Scheduled(fixedRate = 5000)
    private void scanNativeTransaction() {
        Chain chain = chainRepository.findByChainId("TON");
        BigInteger lastIdTransaction = chain.getLastBlockOrTransaction();
        String address = "UQDO25JcIewF1ZkceywwqZljGHCg-d-tVVs-PRAXOjZpyvD1";
        String urlRequest = chain.getRpc() + "/accounts/"+address+"/events?limit=20";
        if (lastIdTransaction != null) urlRequest += "&start_date=" + lastIdTransaction;
        String jsonResponse = okHttp.get(urlRequest);
        if (jsonResponse != null) {
            ArrayList<TransactionTon> transactionTons = parseTransactionJson(jsonResponse, address);
            for (TransactionTon transactionTon : transactionTons) {
                if (transactionTon != null) {
                        if (transactionTon.getToken().equals("TON")) transactionTon.setSum(priceCryptoAPI.getPrice("TON")*transactionTon.getSum());
                        sendRequest("completed", transactionTon);
                    }
                }
            }
    }

    private ArrayList<TransactionTon> parseTransactionJson(String json, String address) {
        ArrayList<TransactionTon> transactionTons = new ArrayList<>();
            JSONObject eventObj = new JSONObject(json);
            JSONArray events = eventObj.getJSONArray("events");
            if (chainRepository.findByChainId("TON").getLastBlockOrTransaction()==null){
                chainRepository.updateLastBlockOrTransaction(events.getJSONObject(0).getBigInteger("timestamp").add(new BigInteger("50")), "TON");
            }
            for (int i =0;i<events.length();i++) {
                try {
                    JSONObject event = events.getJSONObject(i);
                if (event.getBigInteger("timestamp").compareTo(chainRepository.findByChainId("TON").getLastBlockOrTransaction().subtract(new BigInteger("50"))) >= 0){
                    chainRepository.updateLastBlockOrTransaction(event.getBigInteger("timestamp").add(new BigInteger("50")), "TON");
                }
                JSONObject action = event.getJSONArray("actions").getJSONObject(0);
                boolean isTransferTonEvent = action.getString("type").equals("TonTransfer");
                boolean isJettonTransferEvent = action.getString("type").equals("JettonTransfer");
                boolean isStatusCompleted = action.getString("status").equals("ok");
                boolean isMyTransfer;
                if (isTransferTonEvent) isMyTransfer = action.getJSONObject("TonTransfer").getJSONObject("sender").getString("address").equals(address);
                else isMyTransfer = action.getJSONObject("JettonTransfer").getJSONObject("sender").getString("address").equals(address);
                String token = "";
                String comment = "";
                if (isTransferTonEvent) {
                    comment = action.getJSONObject("TonTransfer").getString("comment");
                    token = "TON";
                }
                else {
                    comment = action.getJSONObject("JettonTransfer").getString("comment");
                    token = "USDT";
                }
                if ((!isTransferTonEvent && !isJettonTransferEvent) || !isStatusCompleted || isMyTransfer || comment.isEmpty()) continue;
                BigDecimal sum;
                if (isTransferTonEvent) sum = new BigDecimal(action.getJSONObject("TonTransfer").getBigInteger("amount")).divide(new BigDecimal("1000000000"));
                else sum = new BigDecimal(action.getJSONObject("JettonTransfer").getBigInteger("amount")).divide(new BigDecimal("1000000"));
                transactionTons.add(new TransactionTon(comment, sum.doubleValue(), token));
            }
                catch (Exception err){
                    err.printStackTrace();
                }
        }
        return transactionTons;
    }

    @Data
    @AllArgsConstructor
    private class TransactionTon{
        private String comment;
        private double sum;
        private String token;

    }

    private void sendRequest(String type, TransactionTon transaction){
        Gson gson = new Gson();
        String idClient = transaction.comment;
        okHttp.post("http://localhost:8080/api/callback", gson.toJson(new RequestBody(type,idClient,transaction.sum)), jwtService.generateToken("harasukb@gmail.com"),"Auth");
    }
}
