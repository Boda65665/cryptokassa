package org.example.payservice.Crypto;

import org.example.payservice.HttpRequest.OkHttp;
import org.json.JSONObject;

public class PriceCryptoAPI {
    private final OkHttp okHttp = new OkHttp();

    public double getPrice(String token) {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?symbol="+token+"&convert=USD";
        String key = "9669033c-7f10-44e2-b3e6-a22f24f8cb4c";
        JSONObject jsonObject = new JSONObject(okHttp.get(url, key,"X-CMC_PRO_API_KEY"));
        return jsonObject.getJSONObject("data")
                .getJSONObject(token)
                .getJSONObject("quote")
                .getJSONObject("USD")
                .getDouble("price");
    }
}

