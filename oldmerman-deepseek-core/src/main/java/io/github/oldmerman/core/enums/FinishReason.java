package io.github.oldmerman.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FinishReason {
    STOP("stop"),
    LENGTH("length"),
    CONTENT_FILTER("content_filter"),
    TOOL_CALLS("tool_calls"),
    INSUFFICIENT_SYSTEM_RESOURCE("insufficient_system_resource"),
    ABORTED("aborted")
    ;

    FinishReason(String value){
        this.value = value;
    }

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FinishReason fromValue(String value) {
        for (FinishReason reason : FinishReason.values()) {
            if(reason.value.equals(value)){
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown reason: " + value);
    }

}
