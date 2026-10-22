# oldmerman-deepseek-core

[English](./README.md) | **中文**

> 参考文档：[DeepSeek API Docs](https://api-docs.deepseek.com/zh-cn/)

> 版本需求：`JDK`：21+  

> 包体积：约80kb

最近几天想到`鱼人知识库`快要过期(2026.12.21)，所以想到以后博客的AI服务应该如何提供，精打细算下，打算自己写一套deepseek在`Java`的封装，顺便练练自己的封装能力。这里长话短说，直接进入正题。



## 设计思路

定位：主要用于老鱼人的小型博客，或者以后需要在`Java`中使用AI能力。自己的追求是轻量级，可拓展，该SDK要封装DeepSeek大部分的功能，方便未来基于DeepSeek构建Agent应用。

AI的功能本质是对话，到了互联网，就是请求与响应。SDK封装的主要目的就是简化这一流程，让一行简单代码就能调用大模型，让开发者将更多精力投入自己业务的开发中。



### 主要任务

需要实现的内容不多，主要有以下几点：

* 各种请求和响应的序列化
* 提供工具注册的方式
* 提供商的一些其它接口(比如最近DeepSeek新发布的网页搜索和抓取)

为了实现这些功能，SDK需要依赖`Jackson`和`okHttp`这两个包，前者是**高性能**的序列化工具，后者则是**轻量级**的http客户端，它们极大的简化了**序列化**和**请求与响应**的流程。



### 为什么选择`Jackson`和`okhttp`？

`Jackson`不需要多说，序列化方面个人认为在java生态没有敌手。

至于为什么不选“更未来，更适合AI”的`WebFlux`异步框架而选择`okhttp`，原因有二：

* `WebFlux`比较依赖Spring框架(尽管当前博客基于SpringBoot，并通过WebClient调用Python的AI服务)

* `okhttp`更加轻量，性能也不差，能满足小型应用的需求
  
  

### 实现什么？

这是最根本的问题，老鱼人的设想是一个client，给个`api-key`就能跑起来，支持chat和stream两种主要对话方式。该SDK可能也有些设计和langchain相似，因为我写了一段时间基于langchain和langgraph调用大模型的代码。准备好这些，就开始实现自己的SDK了。

下文主要以**介绍SDK**的使用来展开，怎么实现的？主要是繁琐的序列化流程，这里就不过多赘述了。



## SDK核心——ChatClient

`ChatClient`在`1.0.0`实现，可以通过api-key创建对象，主要有chat，achat，stream三个方法。



创建`DeepSeekConfig`对象，并通过该对象获取对应的client，推荐全局共用一个配置，并保证单例ChatClient。

```java
DeepSeekConfig config = new DeepSeekConfig("your api-key");
config.setXxx(); // 覆盖内部线程池，completionUrl(可选)
ChatClient client = DeepSeekClient.chat(config); // 推荐使用config
```



### CompletionRequest

该对象主要封装了DeepSeek大模型的绝大多数的请求参数，如`max_tokens`，`reason_effort`等，若你想要对大模型应用有更深的理解，从请求入手，理解各字段的含义是很重要的。

```java
    // 模型名称
    private String model; 
    // 消息列表
    private List<Message> messages;
    // 截断内容的token数，当模型生成token达到一定数量直接截断，强烈建议配合提示词约束 
    @JsonProperty("max_token")
    private Integer maxToken; 
    // 响应格式，目前只有text和json
    @JsonProperty("response_format")
    private Map<String, String> responseFormat;
    // 是否使用流式输出，不推荐手动开启
    private Boolean stream;
    // 是否开启思考模式，默认开启
    private Map<String, String> thinking;
    // 思考强度，比如在思考模式下使用，默认为high
    @JsonProperty("reason_effort")
    private ReasonEffort reasonEffort;
    // 模型温度，0<=x<=2，值越小生成越严谨，越大则越开放(不推荐修改)
    private Float temperature;
    // 工具列表，用于向模型提供你的工具
    private List<Map<String, Object>> tools;
    // 工具策略，默认为auto，即模型自己选择是否需要调用工具。
    // none(禁止模型调用)，required(模型必须调用>=1的工具)
    @JsonProperty("tool_choice")
    private ToolChoice toolChoice;
```

注：部分字段如`logbrob`， `n`等不支持。

```java
// 创建请求，大部分推荐使用默认的配置，只输入消息列表即可
CompletionRequest req = CompletionRequest.builder()
                .messages(List.of(
                        new SystemMessage("You are a helpful assistant."),
                        new HumanMessage("Hello!")
                )).build();
```



### CompletionResponse

该对象主要封装非流式请求的结果，通过该对象可以很简单的获取此次模型的响应信息，调用消耗等等。构建各种复杂智能体，更推荐使用该对象，因为它相较于流式更简单更可控，当然，如果是用户聊天对话这种场景，还是选择追求首字的流式吧。



下面就简单介绍该对象的各个参数：

```java
    private String id; // 请求的唯一id
    private String created; // 时间
    private String model; // 使用的模型
    // 返回的结果列表(长度比为一，因为不支持n参数，每轮调用只会有一个choice)
    // 但注意其中可能会有多个toolcall请求，需要按顺序放到一个列表中返回
    private List<Choice> choices; 
    private Usage usage; // 模型消耗
    @JsonProperty("system_fingerprint")
    private String systemFingerprint; // 服务端指纹
```



### chat方法

chat会阻塞主线程，但好消息是，DeepSeek的吐字速度能到150tok/s，非常可观，大部分任务不会阻塞太长时间。

可以直接传入字符串调用，也可以通过`CompletionRequest`更精细化调用，该方法和achat, stream两个方法都会提供`List<Message>`和`CompletionRequest`的重载，下文不过多赘述。

```java
CompletionResponse response = client.chat("oldmerman is 21 years old now, how old are oldmerman ten years later?");
```



打印结果，详细的响应对象见上文：

```java
System.out.println("最终结果: " + response.finalResponse());
```



### achat方法

该方法和chat没有太大区别，只是丢给了虚拟线程执行，可以使用自定义虚拟线程覆盖。

后续在future中处理结果`CompletionResponse`即可：

```java
client.achat(List.of(new HumanMessage("o is 12, 3o + 1 = ?")))
      .thenAccept(r -> {
            Choice choice = r.getChoices().get(0);
            System.out.println("AI result: " + choice.message().content());
            System.out.println("Finish Reason: " + choice.finishReason());
      })
      .exceptionally(e -> null);
```



### stream方法

stream常用于交互式场景，让用户早点看到输出的信息，提升用户体验。

SDK封装了回调方法，并传回句柄供调用方随时停止流式生成，让全过程尽量可控：

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
        System.out.println("流结束：" + finishReason);
        System.out.println("Content: " + content);
        System.out.println("ReasonContent: " + reasonContent);
    }

    @Override
    public void onComplete() {
        System.out.println("[DONE]流完成");
    }
});
```

注：若调用方在前端，可以使用适配器模式，在回调中将结果透传。



## 功能集成

### 工具调用

SDK提供了`ToolRegister`注册工具，`@Tool`描述工具信息，`@Param`描述参数信息



注册工具：

```java
ToolRegistry toolRegistry = new ToolRegistry();
        toolRegistry.register(new WeatherTools());
        toolRegistry.register(new OldmermanTools());
```



获取工具信息(用于加入请求上下文)：

```java
toolRegistry.toToolSpecs() // 将结果加入CompletionRequest的tool字段中即可，会处理序列化并保证模型可见
```



工具定义参考：

返回值会自动序列化为JSON，请求参数若为对象，需要将`@Param`写在对象的字段上。

```java
public class WeatherTools {

    public record WeatherQuery(@Param(description = "需要查询的城市") String city, int days, Unit unit) {}
    public enum Unit { C, F }

    @Tool(name = "get_weather", description = "查询指定城市未来几天的天气")
    public WeatherResult getWeather(WeatherQuery q) {
        return new WeatherResult(q.city(), q.days(), 8, "晴");
    }

    public record WeatherResult(@Param(description = "城市名") String city, int days, int temp, String condition) {}
}
```



### 客户端部分接口

调用provider获取：

```java
ProviderClient providerClient = DeepSeekClient.provider(config);
```



方法参考：

| 方法名     | 参数  | 返回值(见源码)          | 介绍     |
|:-------:|:---:|:-----------------:|:------:|
| models  | 无   | ModelListResponse | 获取模型列表 |
| balance | 无   | BillingResponse   | 获取剩余金额 |

由于方法返回值较为简单，使用时直接查看即可，这里不过多赘述。



### WebSearch网页搜索功能

通过调用search获取对应client：

```java
SearchClient searchClient = DeepSeekClient.search(config);
```



方法参考：

| 方法名    | 参数                       | 返回值            | 介绍   |
|:------:|:------------------------:|:--------------:|:----:|
| search | SearchRequest/String(重载) | SearchResponse | 网页搜索 |
| fetch  | String url               | FetchResult    | 网页抓取 |

`fetch`通过提供url抓取对应网页的markdown内容，返回值包括`title`(网页名称)，`content`(md内容)



#### SearchRequest

`SearchRequest`用于构建websearch请求，可以配合`SearchQuery`使用

```java
    private String query; // 必填，需要查询的字符
    private Integer count; // 查询的数量
    @JsonProperty("ori_question") 
    private String oriQuestion; // 用户原始问题 
    private String localization; // 优先返回本地化搜索的ip地址
    @JsonProperty("need_content")
    private Boolean needContent; // 是否返回原网页的正文
```

SearchQuery搜索语法建议参照官方文档：[网页搜索与抓取 | DeepSeek API Docs](https://api-docs.deepseek.com/zh-cn/guides/search/#%E6%90%9C%E7%B4%A2%E8%AF%AD%E6%B3%95)



示例：

```java
SearchQuery query = SearchQuery.builder()
        .after("2025-01-10")
        .before("2026-06-10")
        .lang(List.of("cn", "en"))
        .limitSites(List.of("github.com"))
        .excludeSites(List.of("csdn.net"))
        .build();

String q = query.toQuery("获取几个GitHub上新的热门项目");
System.out.println(q);
SearchResponse res = searchClient.search(SearchRequest.builder().query(q).build());
```



## 最后

上述功能即当前SDK实现的主要功能，已经覆盖了DeepSeek官方除FilesAPI中几乎所有的接口，后续会随官方文档更新内容。

![AGI](C:\Users\asus\Desktop\博客部署\文章\tec_default\Java\oldmerman-deepseek\assets\AGI.jpg)


