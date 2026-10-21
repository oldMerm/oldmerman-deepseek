package io.github.oldmerman.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ImageDetail {
    LOW("low"),
    ORIGINAL("original"),
    AUTO("auto") // auto -> original
    ;

    ImageDetail(String value){
        this.value = value;
    }

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

}
