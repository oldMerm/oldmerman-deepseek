package io.github.oldmerman.core.api.provider.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ModelInfo(
        String id,
        String object,
        String name,
        @JsonProperty("context_window") Integer contextWindow,
        @JsonProperty("max_output_tokens") Integer maxOutputTokens,
        @JsonProperty("input_modalities") List<String> inputModalities,
        @JsonProperty("output_modalities") List<String> outputModalities
){}
