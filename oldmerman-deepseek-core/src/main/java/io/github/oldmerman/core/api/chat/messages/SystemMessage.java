package io.github.oldmerman.core.api.chat.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.oldmerman.core.enums.Role;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SystemMessage(Role role, String content, String name) implements Message {

    public SystemMessage(String content) {
        this(Role.SYSTEM, content, null);
    }
}
