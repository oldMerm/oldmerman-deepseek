package io.github.oldmerman.core.api.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.oldmerman.core.api.chat.messages.AiMessage;
import io.github.oldmerman.core.enums.FinishReason;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Choice(Integer index, AiMessage message,
                     @JsonProperty("finish_reason") FinishReason finishReason) {
}
