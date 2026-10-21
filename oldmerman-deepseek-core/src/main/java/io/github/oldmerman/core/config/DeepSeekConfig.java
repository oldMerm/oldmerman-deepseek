package io.github.oldmerman.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.core.api.chat.pool.AiExecutor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class DeepSeekConfig {

    private static final String BASE_URL = "https://api.deepseek.com";
    private static final String CHAT_PATH = "/chat/completions";
    private static final String TOOLS_PATH = "/v1/tools";
    private static final String BALANCE_PATH = "/user/balance";
    private static final String MODELS_PATH = "/models";

    private final String apiKey;
    private final HttpUrl baseUrl;
    private HttpUrl baseCompletionUrl;
    private OkHttpClient httpClient;
    private ObjectMapper mapper;
    private ExecutorService executorService;

    public DeepSeekConfig(String apiKey){
        this.apiKey = apiKey;
        this.baseUrl = HttpUrl.parse(BASE_URL);
        assert baseUrl != null;
        this.baseCompletionUrl = baseUrl.newBuilder().addPathSegment(CHAT_PATH).build();
        this.httpClient = new OkHttpClient().newBuilder()
                .readTimeout(Duration.ofSeconds(120))
                .writeTimeout(Duration.ofSeconds(20))
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.mapper = new ObjectMapper();
        this.executorService = AiExecutor.executor();
    }

    public String apiKey(){
        return apiKey;
    }

    public OkHttpClient httpClient(){
        return httpClient;
    }

    public ObjectMapper mapper(){
        return mapper;
    }

    public ExecutorService executorService(){
        return executorService;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setBaseCompletionUrl(HttpUrl baseCompletionUrl){
        this.baseCompletionUrl = baseCompletionUrl;
    }

    // url getter
    public HttpUrl baseCompletionUrl(){
        return baseCompletionUrl;
    }

    public HttpUrl searchUrl(){
        return baseUrl.newBuilder().addPathSegment(TOOLS_PATH).addPathSegment("/search").build();
    }

    public HttpUrl fetchUrl() {
        return baseUrl.newBuilder().addPathSegment(TOOLS_PATH).addPathSegment("/fetch").build();
    }

    public HttpUrl balanceUrl(){
        return baseUrl.newBuilder().addPathSegment(BALANCE_PATH).build();
    }

    public HttpUrl modelsUrl(){
        return baseUrl.newBuilder().addPathSegment(MODELS_PATH).build();
    }
}
