package io.github.oldmerman.core.api.chat.param;

import com.fasterxml.jackson.annotation.*;
import io.github.oldmerman.core.api.chat.messages.HumanMessage;
import io.github.oldmerman.core.api.chat.messages.Message;
import io.github.oldmerman.core.enums.ReasonEffort;
import io.github.oldmerman.core.enums.ResponseFormat;
import io.github.oldmerman.core.enums.ToolChoice;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletionRequest {
    private String model;
    private List<Message> messages;
    @JsonProperty("max_token")
    private Integer maxToken;
    @JsonProperty("response_format")
    private Map<String, String> responseFormat;
    private Boolean stream;
    private Map<String, String> thinking;
    @JsonProperty("reason_effort")
    private ReasonEffort reasonEffort;
    private Float temperature;
    private List<Map<String, Object>> tools;
    @JsonProperty("tool_choice")
    private ToolChoice toolChoice;

    private CompletionRequest() {}

    private CompletionRequest(String model, List<Message> messages, Integer maxToken, ResponseFormat format, Boolean stream,
                             Boolean thinking, ReasonEffort effort, Float temperature, List<Map<String, Object>> tools, ToolChoice toolChoice) {
        this.model = model;
        this.messages = messages;
        this.maxToken = maxToken;
        this.responseFormat = format != null && format.equals(ResponseFormat.JSON) ? Map.of("type", format.getValue()) : null;
        this.stream = stream;
        if(!thinking) this.thinking = Map.of("type","disabled");
        this.reasonEffort = thinking ? effort : null;
        this.temperature = temperature;
        this.tools = tools;
        this.toolChoice = toolChoice;
    }

    public static CompletionRequest defaultObject(String message){
        return CompletionRequest.builder().messages(List.of(new HumanMessage(message))).build();
    }

    public static CompletionRequest defaultObject(List<Message> messages){
        return CompletionRequest.builder().messages(messages).build();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String model = "deepseek-flash";
        private List<Message> messages = null;
        private Integer maxToken = null;
        private ResponseFormat responseFormat = null;
        private Boolean stream = false;
        private Boolean thinking = true;
        private ReasonEffort reasonEffort = null;
        private Float temperature = null;
        private List<Map<String, Object>> tools = null;
        private ToolChoice toolChoice = null;

        public Builder model(String model) { this.model = model; return this; }
        public Builder messages(List<Message> messages) { this.messages = new ArrayList<>(messages); return this; }
        public Builder maxToken(Integer maxToken) { this.maxToken = maxToken; return this; }
        public Builder responseFormat(ResponseFormat responseFormat) { this.responseFormat = responseFormat; return this; }
        public Builder stream(boolean stream) { this.stream = stream; return this; }
        public Builder thinking(boolean thinking) { this.thinking = thinking; return this; }
        public Builder reasonEffort(ReasonEffort effort) { this.reasonEffort = effort; return this; }
        public Builder temperature(float temperature) { this.temperature = temperature; return this; }
        public Builder tools(List<Map<String, Object>> tools) { this.tools = tools; return this; }
        public Builder toolChoice(ToolChoice toolChoice) { this.toolChoice = toolChoice; return this; }

        public CompletionRequest build() {
            if (messages == null) messages = new ArrayList<>();
            return new CompletionRequest(model, messages, maxToken, responseFormat,stream, thinking, reasonEffort, temperature, tools, toolChoice);
        }
    }


    public void addMessage(Message message){
        this.messages.add(message);
    }

    public <T extends Message> void addMessage(List<T> message){
        this.messages.addAll(message);
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setMaxToken(Integer maxToken) {
        this.maxToken = maxToken;
    }

    public Integer getMaxToken() {
        return maxToken;
    }

    public void setResponseFormat(Map<String, String> responseFormat) {
        this.responseFormat = responseFormat;
    }

    public Map<String, String> getResponseFormat() {
        return responseFormat;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Map<String, String> getThinking() {
        return thinking;
    }

    public void setThinking(Map<String, String> thinking) {
        this.thinking = thinking;
    }

    public ReasonEffort getReasonEffort() {
        return reasonEffort;
    }

    public void setReasonEffort(ReasonEffort reasonEffort) {
        this.reasonEffort = reasonEffort;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public List<Map<String, Object>> getTools() {
        return tools;
    }

    public void setTools(List<Map<String, Object>> tools) {
        this.tools = tools;
    }

    public ToolChoice getToolChoice() {
        return toolChoice;
    }

    public void setToolChoice(ToolChoice toolChoice) {
        this.toolChoice = toolChoice;
    }
}
