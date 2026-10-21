package io.github.oldmerman.core.api.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ToolCall(Integer index, String id, String type, Map<String, String> function) {

}
