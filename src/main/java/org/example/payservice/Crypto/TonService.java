//package org.example.payservice.Crypto;
//
//import org.example.payservice.Entity.Chain;
//import org.example.payservice.HttpRequest.OkHttp;
//import org.example.payservice.Repositories.ChainRepository;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.ArrayList;
//
//@Component
//public class TonService {
//    private final OkHttp okHttp = new OkHttp();
//    private final PriceCryptoAPI priceCryptoAPI = new PriceCryptoAPI();
//    private final ChainRepository chainRepository;
//
//    public TonService(ChainRepository chainRepository) {
//        this.chainRepository = chainRepository;
//    }
//
//    @Scheduled(fixedRate = 5000)
//    private void scanNativeTransaction() {
//        Chain chain = chainRepository.findByChainId("TON");
//        BigInteger lastIdTransaction = chain.getLastBlockOrTransaction();
//        String address = "UQDO25JcIewF1ZkceywwqZljGHCg-d-tVVs-PRAXOjZpyvD1";
//        String urlRequest = chain.getRpc() + "/accounts/"+address+"/events?limit=20";
//        if (lastIdTransaction != null) urlRequest += "&start_date=" + lastIdTransaction;
//        String jsonResponse = okHttp.get(urlRequest);
//        if (jsonResponse != null) {
//            ArrayList<TransactionTon> transactionTons = parseTransactionJson(jsonResponse, address);
//            for (TransactionTon transactionTon : transactionTons) {
//                if (transactionTon != null) {
//                        System.out.println("получено: " + transactionTon.sum());
//                    }
//                }
//            }
//    }
//
//    private ArrayList<TransactionTon> parseTransactionJson(String json, String address) {
//        ArrayList<TransactionTon> transactionTons = new ArrayList<>();
//        try {
//            JSONObject eventObj = new JSONObject(json);
//            JSONArray events = eventObj.getJSONArray("events");
//            if (chainRepository.findByChainId("TON").getLastBlockOrTransaction()==null){
//                chainRepository.updateLastBlockOrTransaction(events.getJSONObject(0).getBigInteger("timestamp").add(new BigInteger("50")), "TON");
//            }
//            for (int i =0;i<events.length();i++) {
//                JSONObject event = events.getJSONObject(i);
//                if (event.getBigInteger("timestamp").compareTo(chainRepository.findByChainId("TON").getLastBlockOrTransaction()) >= 0){
//                    chainRepository.updateLastBlockOrTransaction(event.getBigInteger("timestamp").add(new BigInteger("50")), "TON");
//                }
//                JSONObject action = event.getJSONArray("actions").getJSONObject(i);
//                boolean isTransferTonEvent = action.getString("type").equals("TonTransfer");
//                boolean isStatusCompleted = action.getString("status").equals("ok");
//                boolean isMyTransfer = action.getJSONObject("TonTransfer").getJSONObject("sender").getString("address").equals(address);
//                boolean isNotNumberComment;
//                String token = "";
//                int comment = 0;
//                try {
//                        comment = Integer.parseInt(action.getJSONObject("TonTransfer").getString("comment"));
//                        isNotNumberComment = false;
//                } catch (Exception err) {
//                    isNotNumberComment = true;
//                }
//                if (!isTransferTonEvent || !isStatusCompleted || isMyTransfer || isNotNumberComment) continue;
//                BigDecimal sum;
//                sum = new BigDecimal(action.getJSONObject("TonTransfer").getBigInteger("amount")).divide(new BigDecimal("1000000000"));
//                transactionTons.add(new TransactionTon(comment, sum.doubleValue(), token));
//            }
//        }
//        catch (Exception err){
//            err.printStackTrace();
//            return new ArrayList<>();
//        }
//        return transactionTons;
//    }
//
//    private record TransactionTon(int comment, double sum, String token){
//    }
//}
