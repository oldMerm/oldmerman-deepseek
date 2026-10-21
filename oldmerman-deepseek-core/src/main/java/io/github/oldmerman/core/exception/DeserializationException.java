package io.github.oldmerman.core.exception;

public class DeserializationException extends RuntimeException {
    private final String rawResponse;
    public DeserializationException(String rawResponse, Throwable cause) {
        super("Failed to deserialize response: " + rawResponse, cause);
        this.rawResponse = rawResponse;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}
