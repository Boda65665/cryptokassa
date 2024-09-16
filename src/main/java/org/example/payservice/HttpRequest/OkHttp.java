package org.example.payservice.HttpRequest;

import okhttp3.*;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OkHttp {
    private final Logger logger = Logger.getLogger("okHttp");

    private String send(String url,String json,String auth,String authHeader,String method){
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("",mediaType);
        if (json!=null) {
            body = RequestBody.create(json,mediaType);
        }
        Request.Builder builderRequest = new Request.Builder();
        builderRequest.url(url);
        if (authHeader!=null){
            builderRequest.addHeader(authHeader,auth);
        }
        if (method.equals("post")) {
            builderRequest.post(body);
        }
        else builderRequest.get();
        try (Response response = client.newCall(builderRequest.build()).execute()) {
            if (response.body()!=null) {
                return response.body().string();
            }
            else {
                logger.log(Level.WARNING,"Response is null");
                return null;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING,e.toString());
            return null;
        }
    }

    public String get(String url, String auth,String authHeader){
        return send(url, null, auth, authHeader, "get");
    }

    public String get(String url){
        return get(url, null, null);
    }

    public String post(String url,String json,String auth,String authHeader){
        return send(url, json, auth, authHeader, "post");
    }
}
