package io.github.oldmerman.core.api.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.oldmerman.core.api.chat.model.CompletionResponse;
import io.github.oldmerman.core.api.chat.model.ToolCall;
import io.github.oldmerman.core.api.chat.messages.Message;
import io.github.oldmerman.core.api.chat.param.CompletionRequest;
import io.github.oldmerman.core.config.DeepSeekConfig;
import io.github.oldmerman.core.enums.FinishReason;
import io.github.oldmerman.core.exception.*;
import okhttp3.*;
import okio.BufferedSource;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * DeepSeek Chat Client(ChatCompletion API)
 * <p>All the agent in project "oldmerman-deepseek-agent"
 * are base on this client, it provide any methods to use
 * DeepseekAPI include chat, achat(async), stream(async)</p>
 *
 * <p>These three methods have common parameters
 * Which are String, List(Message), CompletionRequest</p>
 * <p>The method all have two overload method, the same return value,
 * the core is method(CompletionRequest req), refer to examples</p>
 *
 * <strong>Examples: </strong>
 * <pre>{@code
 * CompletionResponse response = client.chat("o is 12, 3o + 1 = ?");
 * System.out.println("final response: " + response.finalResponse());
 * System.out.println("total tokens: " + response.getUsage().getTotalTokens());
 *
 * client.achat(List.of(new HumanMessage("o is 12, 3o + 1 = ?")))
 *         .thenAccept(r -> {
 *             Choice choice = r.getChoices().get(0);
 *             System.out.println("AI result: " + choice.getMessage().content());
 *             System.out.println("Finish Reason: " + choice.getFinishReason());
 *         })
 *         .exceptionally(e -> null);
 *
 * CompletionRequest request = CompletionRequest.builder()
 *                   .messages(List.of(new HumanMessage("What's the weather in Dongguan?")))
 *                   .tools(toolRegistry.toToolSpecs())
 *                   .build();
 * DeepseekClient.StreamHandle stream = client.stream(request, new DeepseekClient.ChunkListener() {
 *          String content = "";
 *          String reasonContent = "";
 *
 *          @Override
 *          public void onContent(String chunk) {
 *              content += chunk;
 *          }
 *
 *          @Override
 *          public void onReasoning(String reasoning) {
 *               reasonContent += reasoning;
 *          }
 *
 *          @Override
 *          public void onToolCall(ToolCall toolCall) throws JsonProcessingException {
 *              System.out.println(mapper.writeValueAsString(toolCall));
 *          }
 *
 *          @Override
 *          public void onEnd(FinishReason finishReason) {
 *              System.out.println("End：" + finishReason);
 *              System.out.println("Content: " + content);
 *              System.out.println("ReasonContent: " + reasonContent);
 *          }
 *
 *          @Override
 *          public void onComplete() {
 *              System.out.println("[DONE]Stream Finished);
 *          }
 *      });
 * }</pre>
 *
 * @see <a href="https://platform.deepseek.com">Get an api-key</a>
 * @see <a href="https://api-docs.deepseek.com">Deepseek api doc</a>
 * @author oldmerman
 * @since 1.0.0
 */
public class ChatClient {
    private final DeepSeekConfig config;

    public CompletionResponse chat(String message) {
        return chat(CompletionRequest.defaultObject(message));
    }

    public CompletionResponse chat(List<Message> messages) {
        return chat(CompletionRequest.defaultObject(messages));
    }

    public CompletionResponse chat(CompletionRequest requestObject) {
        Request request = buildRequest(requestObject);

        try (Response response = config.httpClient().newCall(request).execute()) {
            if (response.body() == null) {
                throw new NetworkException("Empty response body", null);
            }

            String responseString = response.body().string();
            if (!response.isSuccessful()) {
                ResultCode.checkResponseCode(response.code(), responseString);
            }
            try {
                CompletionResponse completionResponse = config.mapper().readValue(responseString, CompletionResponse.class);
                FinishReason finishReason = completionResponse.getChoices().get(0).finishReason();
                if (finishReason != FinishReason.STOP && finishReason != FinishReason.TOOL_CALLS) {
                    throw new FinishException(finishReason, completionResponse);
                }
                return completionResponse;
            } catch (JsonProcessingException e) {
                throw new DeserializationException(responseString, e);
            }
        } catch (IOException e) {
            throw new NetworkException("Network error calling " + request.url(), e);
        }
    }

    public CompletableFuture<CompletionResponse> achat(String message) {
        return CompletableFuture.supplyAsync(() -> chat(message), config.executorService());
    }

    public CompletableFuture<CompletionResponse> achat(List<Message> messages) {
        return CompletableFuture.supplyAsync(() -> chat(messages), config.executorService());
    }

    public CompletableFuture<CompletionResponse> achat(CompletionRequest requestObject) {
        return CompletableFuture.supplyAsync(() -> chat(requestObject), config.executorService());
    }


    public StreamHandle stream(String message, ChunkListener listener) {
        return stream(CompletionRequest.defaultObject(message), listener);
    }

    public StreamHandle stream(List<Message> messages, ChunkListener listener) {
        return stream(CompletionRequest.defaultObject(messages), listener);
    }

    public StreamHandle stream(CompletionRequest requestObject, ChunkListener listener) {
        requestObject.setStream(true);

        Request request = buildRequest(requestObject);
        Call call = config.httpClient().newCall(request);
        StreamHandle handle = new StreamHandle(call);

        listener.onStart(handle);

        config.executorService().execute(() -> {
            try (Response response = call.execute()) {

                if (handle.isCancelled()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    ResultCode resultCode = ResultCode.fromCode(response.code());
                    if (resultCode != null) {
                        listener.onError(new IOException("DeepSeek API error: " + resultCode.code() + ", " + resultCode.reason()));
                    }else {
                        String error = response.body() == null ? "" : response.peekBody(8192).string();
                        listener.onError(new IOException("DeepSeek API error: HTTP " + response.code() + ", " + error));
                    }
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    listener.onError(new IOException("Empty response body"));
                    return;
                }

                BufferedSource source = body.source();
                StringBuilder data = new StringBuilder();
                String line;
                boolean done = false;

                while (!handle.isCancelled() && (line = source.readUtf8Line()) != null) {
                    if (line.startsWith(":")) {
                        continue;
                    }

                    if (line.startsWith("data:")) {
                        String value = line.substring(5);
                        if (value.startsWith(" ")) {
                            value = value.substring(1);
                        }

                        if (!data.isEmpty()) {
                            data.append('\n');
                        }
                        data.append(value);
                        continue;
                    }

                    if (line.isEmpty() && !data.isEmpty()) {
                        String payload = data.toString();
                        data.setLength(0);

                        if (dispatchEvent(payload, handle, listener)) {
                            done = true;
                            break;
                        }
                    }
                }

                if (handle.isCancelled()) {
                    return;
                }

                if (!done && !data.isEmpty()) {
                    done = dispatchEvent(data.toString(), handle, listener);
                }

                if (handle.isCancelled()) {
                    return;
                }

                if (done) {
                    listener.onComplete();
                } else {
                    listener.onError(new EOFException("Stream ended before data: [DONE]"));
                }

            } catch (IOException e) {
                if (!handle.isCancelled()) {
                    listener.onError(e);
                }
            } catch (RuntimeException e) {
                if (!handle.isCancelled()) {
                    listener.onError(new IOException("Stream processing failed", e));
                }
            }
        });

        return handle;
    }


    // constructor, setter...
    public ChatClient(String apiKey) {
        this.config = new DeepSeekConfig(apiKey);
    }

    public ChatClient(DeepSeekConfig config) {
        this.config = config;
    }


    // utils...
    private Request buildRequest(CompletionRequest requestBody) {
        String apiKey = config.apiKey();
        String baseUrl = config.baseCompletionUrl().toString();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("apiKey is null, Please get an apiKey from https://platform.deepseek.com");
        }

        if (baseUrl.isBlank()) {
            throw new IllegalStateException("baseUrl is null, check setUrl(...), now: " + config.baseCompletionUrl());
        }

        String jsonBody;
        try {
            jsonBody = config.mapper().writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(
                        jsonBody,
                        MediaType.parse("application/json; charset=utf-8")
                )).build();
    }

    private boolean dispatchEvent(String payload, StreamHandle handle, ChunkListener listener) throws IOException {
        if (handle.isCancelled()) {
            return false;
        }

        if ("[DONE]".equals(payload)) {
            return true;
        }

        dispatchChunk(payload, listener);
        return false;
    }

    private void dispatchChunk(String json, ChunkListener listener) throws IOException {
        JsonNode root = config.mapper().readTree(json);
        if (root == null || root.isNull()) {
            return;
        }

        JsonNode choices = root.path("choices");
        if (!choices.isArray()) {
            return;
        }

        for (JsonNode choice : choices) {
            JsonNode delta = choice.path("delta");

            JsonNode content = delta.get("content");
            if (content != null && content.isTextual()) {
                listener.onContent(content.asText());
            }

            JsonNode reasoning = delta.get("reasoning_content");
            if (reasoning != null && reasoning.isTextual()) {
                listener.onReasoning(reasoning.asText());
            }

            JsonNode toolCalls = delta.path("tool_calls");
            if (toolCalls.isArray()) {
                for (JsonNode node : toolCalls) {
                    Map<String, String> function = new HashMap<>();
                    JsonNode fn = node.path("function");

                    JsonNode name = fn.get("name");
                    if (name != null && !name.isNull()) {
                        function.put("name", name.asText());
                    }

                    JsonNode arguments = fn.get("arguments");
                    if (arguments != null && !arguments.isNull()) {
                        function.put("arguments", arguments.asText());
                    }

                    JsonNode index = node.get("index");

                    ToolCall toolCall = new ToolCall(
                            index != null && index.canConvertToInt()
                                    ? index.asInt() : null,
                            node.hasNonNull("id")
                                    ? node.get("id").asText() : null,
                            node.hasNonNull("type")
                                    ? node.get("type").asText() : null,
                            function
                    );

                    listener.onToolCall(toolCall);
                }
            }

            JsonNode finishReason = choice.get("finish_reason");
            if (finishReason != null && !finishReason.isNull()) {
                listener.onEnd(FinishReason.fromValue(finishReason.asText()));
            }
        }
    }

    // stream event...
    public interface ChunkListener {
        default void onStart(StreamHandle handle) {}

        default void onContent(String chunk) {
        }

        default void onReasoning(String reasoning) {
        }

        default void onToolCall(ToolCall toolCall) throws JsonProcessingException {
        }

        default void onEnd(FinishReason finishReason) {
            if (finishReason != FinishReason.STOP && finishReason != FinishReason.TOOL_CALLS) {
                throw new FinishException(finishReason, null);
            }
        }

        default void onComplete() {
        }

        default void onError(IOException e) {
        }
    }

    public static class StreamHandle {
        private final Call call;
        private volatile boolean cancelled = false;

        StreamHandle(Call call) {
            this.call = call;
        }

        public void cancel() {
            cancelled = true;
            call.cancel();
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

}
