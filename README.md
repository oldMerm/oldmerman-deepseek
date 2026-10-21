# oldmerman-deepseek-core

**English** | [中文](./README.zh.md)

> Reference documentation: [DeepSeek API Docs](https://api-docs.deepseek.com/)

> Version requirement: `JDK`: 21+

> Package size: about 80kb

A few days ago, I realized that the `Merman Knowledge Base` is about to expire (2026.12.21), so I started thinking about how to provide AI services for my blog in the future. After careful budgeting, I decided to write my own `Java` wrapper for DeepSeek, and also practice my encapsulation skills along the way. Let's keep the preamble short and get straight to the point.



## Design Philosophy

Positioning: mainly for Old Merman's small blog, or for future needs to use AI capabilities in `Java`. My pursuit is lightweight and extensible. This SDK should encapsulate most of DeepSeek's functionality, making it convenient to build Agent applications based on DeepSeek in the future.

The essence of AI functionality is conversation; on the internet, that means requests and responses. The main purpose of the SDK encapsulation is to simplify this process, so that a single line of code can call the large model, allowing developers to invest more energy into their own business development.



### Main Tasks

There isn't much to implement, mainly the following:

* Serialization of various requests and responses
* Providing a way to register tools
* Some other interfaces from the provider (such as DeepSeek's recently released web search and fetch)

To implement these features, the SDK needs to depend on `Jackson` and `okHttp`. The former is a **high-performance** serialization tool, and the latter is a **lightweight** HTTP client. They greatly simplify the **serialization** and **request/response** processes.



### Why Choose `Jackson` and `okhttp`?

`Jackson` needs no explanation; in terms of serialization, I personally think it has no rival in the Java ecosystem.

As for why not choose `WebFlux`, an asynchronous framework that is "more future-oriented and more suitable for AI," but instead choose `okhttp`, there are two reasons:

* `WebFlux` relies heavily on the Spring framework (although the current blog is based on SpringBoot and calls Python AI services through WebClient)

* `okhttp` is more lightweight, performs well, and can meet the needs of small applications



### What to Implement?

This is the most fundamental question. Old Merman's vision is a client that can run as soon as an `api-key` is provided, supporting the two main conversation modes: chat and stream. The SDK may also have some design similarities to langchain, because I wrote code for calling large models based on langchain and langgraph for a while. With these prepared, I started implementing my own SDK.

The following mainly introduces the **usage of the SDK**. How is it implemented? Mainly tedious serialization processes, which I won't elaborate on here.



## SDK Core — ChatClient

`ChatClient` was implemented in `1.0.0`. You can create an object with an api-key, and it mainly has three methods: chat, achat, and stream.



Create a `DeepSeekConfig` object and obtain the corresponding client through it. It is recommended to share one configuration globally and ensure a singleton ChatClient.

```java
DeepSeekConfig config = new DeepSeekConfig("your api-key");
config.setXxx(); // Override internal thread pool, completionUrl (optional)
ChatClient client = DeepSeekClient.chat(config); // Recommended to use config
```



### CompletionRequest

This object mainly encapsulates most of the request parameters of the DeepSeek large model, such as `max_tokens`, `reason_effort`, etc. If you want a deeper understanding of large model applications, starting from requests and understanding the meaning of each field is very important.

```java
    // Model name
    private String model; 
    // Message list
    private List<Message> messages;
    // Number of tokens for truncating content; when the model generates a certain number of tokens, it is directly truncated. Strongly recommended to use with prompt constraints
    @JsonProperty("max_token")
    private Integer maxToken; 
    // Response format, currently only text and json
    @JsonProperty("response_format")
    private Map<String, String> responseFormat;
    // Whether to use streaming output; manual enabling is not recommended
    private Boolean stream;
    // Whether to enable thinking mode, enabled by default
    private Map<String, String> thinking;
    // Thinking intensity, for example used in thinking mode, default is high
    @JsonProperty("reason_effort")
    private ReasonEffort reasonEffort;
    // Model temperature, 0<=x<=2; smaller values make generation more rigorous, larger values make it more open (modification not recommended)
    private Float temperature;
    // Tool list, used to provide your tools to the model
    private List<Map<String, Object>> tools;
    // Tool strategy, default is auto, i.e. the model decides whether to call tools.
    // none (forbid the model from calling), required (the model must call >=1 tool)
    @JsonProperty("tool_choice")
    private ToolChoice toolChoice;
```

Note: Some fields such as `logbrob`, `n`, etc. are not supported.

```java
// Create a request. For most cases, it is recommended to use the default configuration and only input the message list
CompletionRequest req = CompletionRequest.builder()
                .messages(List.of(
                        new SystemMessage("You are a helpful assistant."),
                        new HumanMessage("Hello!")
                )).build();
```



### CompletionResponse

This object mainly encapsulates the result of non-streaming requests. Through this object, you can easily obtain the model's response information for this call, consumption, etc. For building various complex agents, this object is more recommended because it is simpler and more controllable than streaming. Of course, for scenarios like user chat conversations, it's better to choose streaming to pursue the first token.

Below is a brief introduction to each parameter of this object:

```java
    private String id; // Unique id of the request
    private String created; // Time
    private String model; // Model used
    // List of returned results (length ratio is one, because the n parameter is not supported; each round of calls produces only one choice)
    // But note that there may be multiple toolcall requests in it, which need to be returned in a list in order
    private List<Choice> choices; 
    private Usage usage; // Model consumption
    @JsonProperty("system_fingerprint")
    private String systemFingerprint; // Server fingerprint
```



### chat Method

chat blocks the main thread, but the good news is that DeepSeek's output speed can reach 150 tok/s, which is very considerable, and most tasks won't block for too long.

You can call it directly by passing a string, or call it more precisely through `CompletionRequest`. This method, along with achat and stream, will provide overloads for `List<Message>` and `CompletionRequest`, which I won't elaborate on below.

```java
CompletionResponse response = client.chat("oldmerman is 21 years old now, how old are oldmerman ten years later?");
```



Print the result. For the detailed response object, see above:

```java
System.out.println("Final result: " + response.finalResponse());
```



### achat Method

This method is not much different from chat; it just delegates to a virtual thread for execution, and you can override it with a custom virtual thread.

Then handle the result `CompletionResponse` in the future:

```java
client.achat(List.of(new HumanMessage("o is 12, 3o + 1 = ?")))
      .thenAccept(r -> {
            Choice choice = r.getChoices().get(0);
            System.out.println("AI result: " + choice.message().content());
            System.out.println("Finish Reason: " + choice.finishReason());
      })
      .exceptionally(e -> null);
```



### stream Method

stream is often used in interactive scenarios to let users see output information earlier and improve user experience.

The SDK encapsulates callback methods and returns a handle for the caller to stop streaming generation at any time, making the whole process as controllable as possible:

```java
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
        System.out.println("[DONE] Stream completed");
    }
});
```

Note: If the caller is on the frontend, you can use the adapter pattern to pass through the results in the callback.



## Feature Integration

### Tool Invocation

The SDK provides `ToolRegister` for registering tools, `@Tool` to describe tool information, and `@Param` to describe parameter information.



Registering tools:

```java
ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new WeatherTools());
        toolRegistry.register(new OldmermanTools());
```



Retrieving tool information (for adding to the request context):

```java
toolRegistry.toToolSpecs() // Add the result to the tool field of CompletionRequest; serialization will be handled and the model will be able to see it
```



Tool definition reference:

Return values are automatically serialized to JSON. If a request parameter is an object, `@Param` needs to be written on the object's fields.

```java
public class WeatherTools {

    public record WeatherQuery(@Param(description = "City to query") String city, int days, Unit unit) {}
    public enum Unit { C, F }

    @Tool(name = "get_weather", description = "Query the weather for a specified city for the next few days")
    public WeatherResult getWeather(WeatherQuery q) {
        return new WeatherResult(q.city(), q.days(), 8, "Sunny");
    }

    public record WeatherResult(@Param(description = "City name") String city, int days, int temp, String condition) {}
}
```



### Some Client Interfaces

Call the provider to obtain:

```java
ProviderClient providerClient = DeepSeekClient.provider(config);
```



Method reference:

| Method Name | Parameters | Return Value (see source code) | Description |
|:-------:|:---:|:-----------------:|:------:|
| models  | None   | ModelListResponse | Get model list |
| balance | None   | BillingResponse   | Get remaining balance |

Since the method return values are relatively simple, you can just look at them when using them, and I won't elaborate here.



### WebSearch Web Search Feature

Obtain the corresponding client by calling search:

```java
SearchClient searchClient = DeepSeekClient.search(config);
```



Method reference:

| Method Name    | Parameters                       | Return Value            | Description   |
|:------:|:------------------------:|:--------------:|:----:|
| search | SearchRequest/String (overload) | SearchResponse | Web search |
| fetch  | String url               | FetchResult    | Web fetch |

`fetch` fetches the markdown content of the corresponding webpage by providing a url. The return value includes `title` (webpage name) and `content` (md content).



#### SearchRequest

`SearchRequest` is used to build websearch requests and can be used with `SearchQuery`.

```java
    private String query; // Required, the string to query
    private Integer count; // Number of queries
    @JsonProperty("ori_question") 
    private String oriQuestion; // User's original question 
    private String localization; // IP address to prioritize localized search results
    @JsonProperty("need_content")
    private Boolean needContent; // Whether to return the main text of the original webpage
```

For SearchQuery search syntax, it is recommended to refer to the official documentation: [Web Search and Fetch | DeepSeek API Docs](https://api-docs.deepseek.com/guides/search/#search-syntax)



Example:

```java
SearchQuery query = SearchQuery.builder()
        .after("2025-01-10")
        .before("2026-06-10")
        .lang(List.of("cn", "en"))
        .limitSites(List.of("github.com"))
        .excludeSites(List.of("csdn.net"))
        .build();

String q = query.toQuery("Get a few new popular projects on GitHub");
System.out.println(q);
SearchResponse res = searchClient.search(SearchRequest.builder().query(q).build());
```



## Finally

The above features are the main functionality currently implemented by the SDK, and have covered almost all of DeepSeek's official interfaces except the FilesAPI. Future updates will follow the official documentation.

![AGI](images/AGI.jpg)