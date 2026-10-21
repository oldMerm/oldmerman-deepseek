package io.github.oldmerman.core.api.chat.pool;

import java.util.concurrent.*;

public final class AiExecutor {

    private static final ExecutorService AI_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            AI_EXECUTOR.shutdown();
            try {
                if (!AI_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {
                    AI_EXECUTOR.shutdownNow();
                }
            } catch (InterruptedException e) {
                AI_EXECUTOR.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }, "ai-executor-shutdown-hook"));
    }

    private AiExecutor() {}

    public static ExecutorService executor() {
        return AI_EXECUTOR;
    }

}