# oldmerman-deepseek-core

**English** | [中文](./README.zh.md)

> Reference: [DeepSeek API Docs](https://api-docs.deepseek.com)

> Requirements: `JDK`: 21+

> Package size: ~80kb

## Design Philosophy

Positioning: Primarily intended for small-scale blogs and for using AI capabilities within `Java` in the future. The goal is a lightweight, extensible SDK that wraps most of DeepSeek's functionality, making it easy to build Agent applications on top of DeepSeek in the future.

The essence of AI functionality is conversation—over the internet, that means requests and responses. The main purpose of an SDK wrapper is to simplify this flow, so that a single line of code can invoke a large model and developers can focus more energy on their own business logic.

### Main Tasks

The scope is modest, mainly the following:

* Serialization of various requests and responses
* Providing a way to register tools
* Other provider interfaces (such as DeepSeek's recently released web search and fetch)

To achieve these, the SDK depends on `Jackson` and `okHttp`—the former a **high-performance** serialization tool, the latter a **lightweight** HTTP client. Together they greatly simplify **serialization** and **request/response** handling.

### Why `Jackson` and `okhttp`?

`Jackson` needs little explanation—in the Java ecosystem, it has no rival for serialization.

As for why not `WebFlux`, the "more future-proof, more AI-suited" async framework, and instead `okhttp`, there are two reasons:

* `WebFlux` depends heavily on the Spring framework (even though the current blog is SpringBoot-based and calls a Python AI service via WebClient)
* `okhttp` is lighter and performs well enough for small applications

### What to Implement?

This is the most fundamental question. The vision is a client that runs with just an `api-key`, supporting chat and stream as the two primary conversation modes. The SDK's design is somewhat similar to langchain, since I spent a while writing code that calls large models based on langchain and langgraph. With that settled, implementation began.

The following sections focus on **introducing SDK usage**. How it's implemented—mostly tedious serialization work—will not be covered in detail.

## SDK Core — ChatClient

`ChatClient` was implemented in `1.0.0`. It can be created via an api-key and mainly provides three methods: chat, achat, and stream.

Create a `DeepSeekConfig` object and obtain the corresponding client through it. It is recommended to share a single config globally and ensure a singleton ChatClient.

    DeepSeekConfig config = new DeepSeekConfig("your api-key");
    config.setXxx(); // Override internal thread pool, completionUrl (optional)
    ChatClient client = DeepSeekClient.chat(config); // Recommended to use config

### CompletionRequest

This object wraps most of the request parameters for the DeepSeek large model, such as `max_tokens`, `reason_effort`, etc. If you want a deeper understanding of large model applications, starting from the request and understanding the meaning of each field is important.

        // Model name
        private String model; 
        // Message list
        private List<Message> messages;
        // Number of tokens for truncating content; when the model generates a certain number of tokens, it is truncated directly. Strongly recommend combining with prompt constraints
        @JsonProperty("max_token")
        private Integer maxToken; 
        // Response format; currently only text and json
        @JsonProperty("response_format")
        private Map<String, String> responseFormat;
        // Whether to use streaming output; manual enabling is not recommended
        private Boolean stream;
        // Whether to enable thinking mode; enabled by default
        private Map<String, String> thinking;
        // Thinking intensity, e.g. used in thinking mode; default is high
        @JsonProperty("reason_effort")
        private ReasonEffort reasonEffort;
        // Model temperature, 0<=x<=2; smaller values generate more rigorously, larger values more openly (modification not recommended)
        private Float temperature;
        // Tool list, used to provide your tools to the model
        private List<Map<String, Object>> tools;
        // Tool policy; default is auto, i.e. the model decides whether to call tools.
        // none (forbid model from calling), required (model must call >=1 tool)
        @JsonProperty("tool_choice")
        private ToolChoice toolChoice;

Note: Some fields such as `logbrob`, `n`, etc. are not supported.

    // Create a request; for most cases the default configuration is recommended—just provide the message list
    CompletionRequest req = CompletionRequest.builder()
                    .messages(List.of(
                            new SystemMessage("You are a helpful assistant."),
                            new HumanMessage("Hello!")
                    )).build();

### CompletionResponse

This object wraps the result of a non-streaming request. Through it you can easily obtain the model's response information, consumption, etc. for this call. For building various complex agents, this object is more recommended because it is simpler and more controllable than streaming. Of course, for user chat scenarios, prefer streaming for faster first-token latency.

A brief introduction to each parameter of this object:

        private String id; // Unique request id
        private String created; // Time
        private String model; // Model used
        // Result list (length is one, since the n parameter is not supported; each call returns only one choice)
        // But note there may be multiple toolcall requests, which need to be returned in a single list in order
        private List<Choice> choices; 
        private Usage usage; // Model consumption
        @JsonProperty("system_fingerprint")
        private String systemFingerprint; // Server-side fingerprint

### chat Method

chat blocks the main thread, but the good news is that DeepSeek's token output speed can reach 150 tok/s, which is very respectable—most tasks won't block for long.

You can call it directly with a string, or use `CompletionRequest` for more fine-grained control. This method, along with achat and stream, provides overloads for `List<Message>` and `CompletionRequest`; these are not repeated below.

    CompletionResponse response = client.chat("oldmerman is 21 years old now, how old are oldmerman ten years later?");

Print the result; see above for the detailed response object:

    System.out.println("Final result: " + response.finalResponse());

### achat Method

This method is not much different from chat—it just delegates to a virtual thread. You can override it with a custom virtual thread.

Then handle the `CompletionResponse` in the future:

    client.achat(List.of(new HumanMessage("o is 12, 3o + 1 = ?")))
          .thenAccept(r -> {
                Choice choice = r.getChoices().get(0);
                System.out.println("AI result: " + choice.message().content());
                System.out.println("Finish Reason: " + choice.finishReason());
          })
          .exceptionally(e -> null);

### stream Method

stream is commonly used in interactive scenarios, letting users see output early and improving UX.

The SDK wraps callback methods and returns a handle so the caller can stop streaming generation at any time, keeping the whole process as controllable as possible:

    CompletionRequest request = CompletionRequest.builder()
            .messages(List.of(new HumanMessage("What's the weather in Dongguan?")))
            .tools(toolRegistry.toToolSpecs())
            .build();
    ChatClient.StreamHandle stream = client.stream(request, new ChatClient.ChunkListener() {
        String content = "";
        String reasonContent = "";
    
        @Override
        public void onContent(String chunk) {
            content += chunk;
        }
    
        @Override
        public void onReasoning(String reasoning) {
            reasonContent += reasoning;
        }
    
        @Override
        public void onToolCall(ToolCall toolCall) throws JsonProcessingException {
            System.out.println(mapper.writeValueAsString(toolCall));
        }
    
        @Override
        public void onEnd(FinishReason finishReason) {
            System.out.println("Stream ended: " + finishReason);
            System.out.println("Content: " + content);
            System.out.println("ReasonContent: " + reasonContent);
        }
    
        @Override
        public void onComplete() {
            System.out.println("[DONE] stream complete");
        }
    });

Note: If the caller is on the frontend, you can use the adapter pattern to pass results through in the callback.

## Feature Integration

### Provider Client Interfaces

Obtain via provider:

    ProviderClient providerClient = DeepSeekClient.provider(config);

Method reference:

| Method | Parameters | Return Value (see source) | Description |
| --- | --- | --- | --- |
| models | None | ModelListResponse | Get model list |
| balance | None | BillingResponse | Get remaining balance |

Since the return values are fairly simple, just inspect them directly when using; they are not detailed here.

### WebSearch Web Search Feature

Obtain the corresponding client via search:

    SearchClient searchClient = DeepSeekClient.search(config);

Method reference:

| Method | Parameters | Return Value | Description |
| --- | --- | --- | --- |
| search | SearchRequest/String (overload) | SearchResponse | Web search |
| fetch | String url | FetchResult | Web page fetch |

`fetch` retrieves the markdown content of the corresponding web page by URL. The return value includes `title` (page name) and `content` (markdown content).

#### SearchRequest

`SearchRequest` is used to build websearch requests and can be used together with `SearchQuery`.

        private String query; // Required; the string to query
        private Integer count; // Number of results
        @JsonProperty("ori_question") 
        private String oriQuestion; // User's original question 
        private String localization; // Prioritize returning localized search IP address
        @JsonProperty("need_content")
        private Boolean needContent; // Whether to return the original page content

For SearchQuery search syntax, refer to the official docs: [Web Search and Fetch | DeepSeek API Docs](https://api-docs.deepseek.com/guides/search#query-operators)

Example:

    SearchQuery query = SearchQuery.builder()
            .after("2025-01-10")
            .before("2026-06-10")
            .lang(List.of("cn", "en"))
            .limitSites(List.of("github.com"))
            .excludeSites(List.of("csdn.net"))
            .build();
    
    String q = query.toQuery("Fetch a few new popular projects on GitHub");
    System.out.println(q);
    SearchResponse res = searchClient.search(SearchRequest.builder().query(q).build());

## Final Notes

The features above are the main functionality currently implemented by the SDK. It already covers almost all DeepSeek official interfaces except the Files API, and will be updated as the official documentation evolves.

![AGI](images/AGI.jpg)