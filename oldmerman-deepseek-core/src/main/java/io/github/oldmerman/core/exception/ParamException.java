package io.github.oldmerman.core.exception;

public class ParamException extends IllegalArgumentException{
    private final int code;
    private final String reason;

    public ParamException(ResultCode resultCode) {
        super("Failed arguments " + resultCode.reason());
        this.reason = resultCode.reason();
        this.code = resultCode.code();
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }
}
