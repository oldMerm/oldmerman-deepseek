package io.github.oldmerman.core.exception;

public enum ResultCode {

    INVALID_REQUEST_BODY(400, "Invalid request body format"),
    API_KEY_ERROR(401, "API key error, authentication failed"),
    INSUFFICIENT_ACCOUNT(402, "Insufficient account balance"),
    REQUEST_WAS_BLOCKED(403, "Fetch request was blocked by a security policy"),
    FAILED_TO_RETRIEVE(404, "Failed to retrieve the available web content"),
    READ_TIMEOUT(408, "Request body read timeout"),
    BODY_EXCEEDS_LIMIT(413, "The request body exceeds the size limit"),
    ERROR_REQUEST_PARAM(422, "Error in request body parameters"),
    REQUEST_EXCEEDING_LIMIT(429, "Request rate or concurrency has reached the limit"),
    SEVER_FAILURE(500, "Internal server failure"),
    SEVER_OVERLOADED(503, "The server is overloaded");

    private final int code;
    private final String reason;

    ResultCode(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int code() {
        return this.code;
    }

    public String reason() {
        return this.reason;
    }

    public static ResultCode fromCode(int code) {
        for (ResultCode rc : values()) {
            if (rc.code == code) return rc;
        }
        return null;
    }

    public static void checkResponseCode(int code, String message) {
        if (code == 200) return;

        ResultCode resultCode = ResultCode.fromCode(code);

        if (resultCode == null) {
            throw new RuntimeException("Unknown fail: code" + code + " body: " + message);
        }

        throw resultCode.toException();
    }

    public RuntimeException toException() {
        return switch (this) {
            case API_KEY_ERROR, INSUFFICIENT_ACCOUNT -> new AccountException(this);
            case INVALID_REQUEST_BODY, FAILED_TO_RETRIEVE, REQUEST_WAS_BLOCKED,
                    READ_TIMEOUT, BODY_EXCEEDS_LIMIT, ERROR_REQUEST_PARAM -> new ParamException(this);
            case REQUEST_EXCEEDING_LIMIT, SEVER_OVERLOADED, SEVER_FAILURE -> new ServerException(this);
        };
    }
}
