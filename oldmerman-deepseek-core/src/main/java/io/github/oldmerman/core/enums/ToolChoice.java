package io.github.oldmerman.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ToolChoice {
    NONE("none"),
    AUTO("auto"),
    REQUIRED("required");

    ToolChoice(String value){
        this.value = value;
    }

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

}
