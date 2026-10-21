package io.github.oldmerman.core.api.chat.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.oldmerman.core.api.chat.model.ToolCall;
import io.github.oldmerman.core.enums.Role;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiMessage(Role role, String content, String name,
                        @JsonProperty("reasoning_content") String reasoningContent,
                        @JsonProperty("tool_calls") List<ToolCall> toolCalls) implements Message {


}