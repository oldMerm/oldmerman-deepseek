package io.github.oldmerman.core.exception;

public class ServerException extends RuntimeException{

    private final int code;
    private final String reason;

    public ServerException(ResultCode resultCode) {
        super("Server error: " + resultCode.reason());
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
