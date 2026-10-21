package io.github.oldmerman.core.api.chat.messages;

import io.github.oldmerman.core.enums.Role;


public sealed interface Message permits SystemMessage, HumanMessage, AiMessage, ToolMessage{

    String content();

    Role role();
}
