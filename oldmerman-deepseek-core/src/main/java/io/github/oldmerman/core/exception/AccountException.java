package io.github.oldmerman.core.exception;

public class AccountException extends RuntimeException{
    private final int code;
    private final String reason;

    public AccountException(ResultCode resultCode) {
        super("Account failed: " + resultCode.reason());
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
