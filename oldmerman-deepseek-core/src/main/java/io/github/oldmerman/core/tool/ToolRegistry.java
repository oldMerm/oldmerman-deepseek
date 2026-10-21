package io.github.oldmerman.core.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToolRegistry {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ToolDef> tools = new LinkedHashMap<>();

    public ToolRegistry register(Object bean) {
        for (Method m : bean.getClass().getDeclaredMethods()) {
            Tool tool = m.getAnnotation(Tool.class);
            if (tool == null) continue;
            ToolDef toolDef = tools.putIfAbsent(tool.name(), new ToolDef(bean, m, tool));
            if (toolDef != null) {
                throw new IllegalStateException(
                        "Duplicate tool name: '" + tool.name() + "' "
                                + "conflicts between " + toolDef.bean().getClass().getName()
                                + "#" + toolDef.method().getName()
                                + " and " + bean.getClass().getName() + "#" + m.getName());
            }
        }
        return this;
    }

    public List<Map<String, Object>> toToolSpecs() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ToolDef def : tools.values()) {
            list.add(Map.of(
                    "type", "function",
                    "function", Map.of(
                            "name", def.tool.name(),
                            "description", def.tool.description(),
                            "parameters", buildParameters(def.method)
                    )
            ));
        }
        return list;
    }

    private Map<String, Object> buildParameters(Method method) {
        Parameter[] params = method.getParameters();
        Type[] genericTypes = method.getGenericParameterTypes();

        if (params.length == 1 && isComplexType(genericTypes[0])) {
            return JsonSchemaMapper.toSchema(genericTypes[0]);
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            Param anno = params[i].getAnnotation(Param.class);
            if (anno == null) continue;
            Map<String, Object> schema = JsonSchemaMapper.toSchema(genericTypes[i]);
            if (!anno.description().isEmpty()) {
                schema.put("description", anno.description());
            }
            properties.put(params[i].getName(), schema);
            if (anno.required()) required.add(params[i].getName());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "object");
        result.put("properties", properties);
        if (!required.isEmpty()) result.put("required", required);
        return result;
    }

    private boolean isComplexType(Type t) {
        if (!(t instanceof Class<?> c)) return true;
        return !(c.isPrimitive() || c == String.class
                || Number.class.isAssignableFrom(c)
                || c == Boolean.class || c.isEnum());
    }

    public String execute(String toolName, String argumentsJson) {
        ToolDef def = tools.get(toolName);
        if (def == null) return "{\"error\":\"unknown tool: " + toolName + "\"}";
        try {
            Object result = invoke(def, argumentsJson);
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private Object invoke(ToolDef def, String argumentsJson) throws Exception {
        Method method = def.method;
        Type[] paramTypes = method.getGenericParameterTypes();
        Object[] args = new Object[paramTypes.length];

        if (paramTypes.length == 0) {
            warnIfUnexpectedArgs(def, argumentsJson);
        }
        else if (paramTypes.length == 1 && isComplexType(paramTypes[0])) {
            args[0] = mapper.readValue(argumentsJson,
                    mapper.constructType(paramTypes[0]));
        } else {
            Map<String, Object> map = mapper.readValue(argumentsJson, new TypeReference<>(){});
            Parameter[] params = method.getParameters();
            for (int i = 0; i < params.length; i++) {
                Object v = map.get(params[i].getName());
                args[i] = mapper.convertValue(v,
                        mapper.constructType(paramTypes[i]));
            }
        }
        method.setAccessible(true);
        return method.invoke(def.bean, args);
    }

    private void warnIfUnexpectedArgs(ToolDef def, String argumentsJson) {
        if (argumentsJson == null) return;
        String s = argumentsJson.trim();
        if (s.isEmpty() || "{}".equals(s) || "null".equals(s)) return;
    }

    private record ToolDef(Object bean, Method method, Tool tool) {}
}