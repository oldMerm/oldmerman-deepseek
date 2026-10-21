package io.github.oldmerman.core.api.utils;

import io.github.oldmerman.core.config.DeepSeekConfig;
import io.github.oldmerman.core.enums.ServiceProvider;
import io.github.oldmerman.core.exception.NetworkException;
import io.github.oldmerman.core.exception.ResultCode;
import okhttp3.*;

import java.io.IOException;

public class RequestUtil {

    public static String buildAndRequestProvider(DeepSeekConfig config, ServiceProvider service, String jsonBody) {
        HttpUrl url;
        boolean isPost = false;
        switch (service){
            case BALANCE -> url = config.balanceUrl();
            case MODELS -> url = config.modelsUrl();
            case SEARCH -> {
                url = config.searchUrl();
                isPost = true;
            }
            case FETCH -> {
                url = config.fetchUrl();
                isPost = true;
            }
            default -> throw new IllegalArgumentException("Unknown server provide" + service);
        }

        // build request
        Request request;
        if (isPost) {
            request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + config.apiKey())
                    .url(url)
                    .post(RequestBody.create(
                            jsonBody,
                            MediaType.parse("application/json; charset=utf-8")
                    ))
                    .build();
        } else {
            request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + config.apiKey())
                    .url(url)
                    .build();
        }

        // request deepseek provider
        try (Response response = config.httpClient().newCall(request).execute()) {
            if (response.body() == null) {
                throw new NetworkException("Empty response body", null);
            }
            String responseString = response.body().string();
            if (!response.isSuccessful()) {
                ResultCode.checkResponseCode(response.code(), responseString);
            }
            return responseString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
