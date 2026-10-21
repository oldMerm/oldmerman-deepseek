package io.github.oldmerman.core.exception;

public class NetworkException extends RuntimeException {
    public NetworkException(String message, Throwable e) {
        super(message, e);
    }
}
