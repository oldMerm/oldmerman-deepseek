package io.github.oldmerman.core.api.chat.param;

public record TextPart(String type, String text) implements ContentPart{

    public TextPart(String text){
        this("text", text);
    }

    public static TextPart from(String text) {
        return new TextPart(text);
    }
}
