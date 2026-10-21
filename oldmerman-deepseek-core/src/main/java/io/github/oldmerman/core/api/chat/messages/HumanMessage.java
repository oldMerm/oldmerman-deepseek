package io.github.oldmerman.core.api.chat.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.oldmerman.core.api.chat.param.ContentPart;
import io.github.oldmerman.core.api.chat.param.TextPart;
import io.github.oldmerman.core.enums.Role;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HumanMessage(Role role, @JsonProperty("content") List<ContentPart> contents, String name) implements Message {

    public HumanMessage(String content){
        this(List.of(new TextPart(content)));
    }

    public HumanMessage(List<ContentPart> contentPartList){
        this(Role.HUMAN, contentPartList, null);
    }

    @Override
    public String content() {
        return contents.get(contents.size() - 1).toString();
    }
}
