package io.github.oldmerman.core.api.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oldmerman.core.api.search.model.FetchResult;
import io.github.oldmerman.core.api.search.model.SearchResponse;
import io.github.oldmerman.core.api.search.param.SearchRequest;
import io.github.oldmerman.core.api.utils.RequestUtil;
import io.github.oldmerman.core.config.DeepSeekConfig;
import io.github.oldmerman.core.enums.ServiceProvider;
import io.github.oldmerman.core.exception.DeserializationException;

public class SearchClient {

    private final DeepSeekConfig config;

    public SearchResponse search(String query) {
        return search(SearchRequest.builder().query(query).build());
    }

    public SearchResponse search(SearchRequest request) {
        String query = request.getQuery();
        int codePointCount = query.codePointCount(0, query.length());
        if (codePointCount < 1 || codePointCount > 128) {
            throw new IllegalArgumentException("Param query's length invalid(from 1 to 128)");
        }
        if (query.trim().isEmpty()) {
            throw new IllegalArgumentException("Param 'query' is null in request");
        }

        String requestBody = null;
        String responseBody = null;
        ObjectMapper mapper = config.mapper();
        try{
            requestBody = mapper.writeValueAsString(request);
            responseBody = RequestUtil.buildAndRequestProvider(config, ServiceProvider.SEARCH, requestBody);
            return mapper.readValue(responseBody, SearchResponse.class);
        }catch (JsonProcessingException e) {
            throw new DeserializationException("Serialization error, requestBody: "
                    + requestBody + " responseBody: " + responseBody, e);
        }
    }

    public FetchResult fetch(String url) {
        String responseBody = null;
        try{
            responseBody = RequestUtil.buildAndRequestProvider(config, ServiceProvider.FETCH, "{\"url\": \"%s\"}".formatted(url));
            return config.mapper().readValue(responseBody, FetchResult.class);
        } catch (JsonProcessingException e) {
            throw new DeserializationException("Serialization error, responseBody: " + responseBody, e);
        }
    }

    public SearchClient(DeepSeekConfig config) {
        this.config = config;
    }
}
