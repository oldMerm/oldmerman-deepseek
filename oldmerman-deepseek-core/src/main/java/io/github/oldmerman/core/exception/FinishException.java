package io.github.oldmerman.core.exception;

import io.github.oldmerman.core.api.chat.model.CompletionResponse;
import io.github.oldmerman.core.enums.FinishReason;

public class FinishException extends RuntimeException{
    private final FinishReason reason;
    private final CompletionResponse errResponse;

    public FinishException(FinishReason reason, CompletionResponse response){
        super(buildMessage(reason));
        this.errResponse = response;
        this.reason = reason;
    }

    private static String buildMessage(FinishReason reason) {
        return switch (reason) {
            case LENGTH -> "Truncated due to exceeding the context window";
            case CONTENT_FILTER -> "Output blocked by content filtering";
            case INSUFFICIENT_SYSTEM_RESOURCE -> "Insufficient system inference resources";
            case ABORTED -> "Generation interrupted";
            default -> "Generation finished abnormally: " + reason.getValue();
        };
    }

    public CompletionResponse getErrResponse() {
        return errResponse;
    }

    public FinishReason getReason() {
        return reason;
    }
}
