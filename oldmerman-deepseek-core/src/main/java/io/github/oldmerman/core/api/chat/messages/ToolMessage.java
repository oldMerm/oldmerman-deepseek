package io.github.oldmerman.core.api.chat.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.oldmerman.core.enums.Role;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolMessage(Role role,
                          @JsonProperty("tool_call_id")String toolCallId, String content) implements Message {

    public ToolMessage(String toolCallId, String content){
        this(Role.TOOL, toolCallId, content);
    }
}

