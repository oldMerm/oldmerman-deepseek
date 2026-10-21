package io.github.oldmerman.core.api.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.oldmerman.core.api.provider.model.BillingResponse;
import io.github.oldmerman.core.api.provider.model.ModelListResponse;
import io.github.oldmerman.core.api.utils.RequestUtil;
import io.github.oldmerman.core.config.DeepSeekConfig;
import io.github.oldmerman.core.enums.ServiceProvider;
import io.github.oldmerman.core.exception.DeserializationException;
import io.github.oldmerman.core.exception.NetworkException;
import io.github.oldmerman.core.exception.ResultCode;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * DeepSeek Provider Client
 * <p>get models, balance info...</p>
 *
 * You should provide your api-key in config then use the client
 * @author oldmerman
 * @since 1.0.2
 */
public class ProviderClient {

    private final DeepSeekConfig config;

    public ModelListResponse models() {
        String body = RequestUtil.buildAndRequestProvider(config, ServiceProvider.MODELS, null);
        try{
            return config.mapper().readValue(body, ModelListResponse.class);
        } catch (JsonProcessingException e) {
            throw new DeserializationException(body, e);
        }
    }

    public BillingResponse balance() {
        String body = RequestUtil.buildAndRequestProvider(config, ServiceProvider.BALANCE, null);
        try{
            return config.mapper().readValue(body, BillingResponse.class);
        } catch (JsonProcessingException e){
            throw new DeserializationException(body, e);
        }
    }


    public ProviderClient(String apiKey) {
        this.config = new DeepSeekConfig(apiKey);
    }

    public ProviderClient(DeepSeekConfig config) {
        this.config = config;
    }
}