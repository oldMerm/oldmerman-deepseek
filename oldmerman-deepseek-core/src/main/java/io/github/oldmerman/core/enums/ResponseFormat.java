package io.github.oldmerman.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResponseFormat {
    TEXT("text"), JSON("json_object");

    private final String value;

    ResponseFormat(String value){
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
