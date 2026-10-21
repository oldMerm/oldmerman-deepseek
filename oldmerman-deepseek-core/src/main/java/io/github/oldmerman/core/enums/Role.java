package io.github.oldmerman.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    SYSTEM("system"),
    HUMAN("user"),
    ASSISTANT("assistant"),
    TOOL("tool"),
    LATEST_REMINDER("latest_reminder")
    ;

    private final String value;

    Role(String value){
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if(role.value.equals(value)){
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
