package io.github.oldmerman.core.api.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Usage(@JsonProperty("prompt_tokens") int promptTokens,
                    @JsonProperty("completion_tokens") int completionTokens,
                    @JsonProperty("total_tokens") int totalTokens,
                    @JsonProperty("prompt_cache_hit_tokens") int promptCacheHitTokens,
                    @JsonProperty("prompt_cache_miss_tokens") int promptCacheMissTokens) {
}
