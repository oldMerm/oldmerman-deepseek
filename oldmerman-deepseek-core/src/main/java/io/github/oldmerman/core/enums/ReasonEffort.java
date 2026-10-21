package io.github.oldmerman.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReasonEffort {
    NONE("none"), LOW("low"), HIGH("high"), MAX("max");

    private final String value;

    ReasonEffort(String value){
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
