package io.github.oldmerman.core.tool;

import io.github.oldmerman.core.tool.Param;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public final class JsonSchemaMapper {

    private JsonSchemaMapper(){}

    public static Map<String, Object> toSchema(Type type){
        return toSchema(type, new HashSet<>());
    }

    private static Map<String, Object> toSchema(Type type, Set<Class<?>> seen) {
        Map<String, Object> schema = new LinkedHashMap<>();

        if (type instanceof WildcardType wt) {
            Type[] upper = wt.getUpperBounds();
            return toSchema(upper.length > 0 ? upper[0] : Object.class, seen);
        }

        if (type instanceof GenericArrayType gat) {
            schema.put("type", "array");
            schema.put("items", toSchema(gat.getGenericComponentType(), seen));
            return schema;
        }
        
        if (type instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            Type[] args = pt.getActualTypeArguments();
            
            if (Collection.class.isAssignableFrom(raw)) {
                schema.put("type", "array");
                Type elem = args.length > 0 ? args[0] : Object.class;
                schema.put("items", toSchema(elem, seen));
            }
            
            if (Map.class.isAssignableFrom(raw)) {
                schema.put("type", "object");
                Type value = args.length > 1 ? args[1] : Object.class;
                schema.put("additionalProperties", toSchema(value, seen));
                return schema;
            }
            
            return toSchema(raw, seen);
        }
        
        if (type instanceof Class<?> clazz) {
            return classToSchema(clazz, seen);
        }
        
        schema.put("type", "string");
        return schema;
    }

    private static Map<String, Object> classToSchema(Class<?> clazz, Set<Class<?>> seen) {
        Map<String, Object> schema = new LinkedHashMap<>();

        if (clazz == String.class || clazz == char.class || clazz == Character.class) {
            schema.put("type", "string");
            return schema;
        }
        if (clazz == int.class || clazz == Integer.class
                || clazz == long.class || clazz == Long.class
                || clazz == short.class || clazz == Short.class
                || clazz == byte.class || clazz == Byte.class
                || clazz == BigInteger.class) {
            schema.put("type", "integer");
            return schema;
        }
        if (clazz == double.class || clazz == Double.class
                || clazz == float.class || clazz == Float.class
                || clazz == BigDecimal.class) {
            schema.put("type", "number");
            return schema;
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            schema.put("type", "boolean");
            return schema;
        }

        if (clazz == LocalDate.class) {
            schema.put("type", "string");
            schema.put("format", "date");
            return schema;
        }
        if (clazz == LocalDateTime.class || clazz == Instant.class || clazz == Date.class) {
            schema.put("type", "string");
            schema.put("format", "date-time");
            return schema;
        }

        if (clazz.isEnum()) {
            schema.put("type", "string");
            Object[] constants = clazz.getEnumConstants();
            List<String> names = new ArrayList<>(constants.length);
            for (Object c : constants) names.add(((Enum<?>) c).name());
            schema.put("enum", names);
            return schema;
        }

        if (clazz.isArray()) {
            schema.put("type", "array");
            schema.put("items", toSchema(clazz.getComponentType(), seen));
            return schema;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            schema.put("type", "array");
            schema.put("items", Map.of("type", "string"));
            return schema;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            schema.put("type", "object");
            return schema;
        }

        if (seen.contains(clazz)) {
            return Map.of("type", "object");
        }
        seen.add(clazz);
        try {
            Map<String, Object> props = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();

            if (clazz.isRecord()) {
                for (RecordComponent rc : clazz.getRecordComponents()) {
                    Map<String, Object> fieldSchema = toSchema(rc.getGenericType(), seen);
                    applyParam(rc, fieldSchema, rc.getName(), required);
                    props.put(rc.getName(), fieldSchema);
                }
            } else {
                for (Field f : clazz.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
                    Map<String, Object> fieldSchema = toSchema(f.getGenericType(), seen);
                    applyParam(f, fieldSchema, f.getName(), required);
                    props.put(f.getName(), fieldSchema);
                }
            }

            Map<String, Object> objSchema = new LinkedHashMap<>();
            objSchema.put("type", "object");
            objSchema.put("properties", props);
            if (!required.isEmpty()) objSchema.put("required", required);
            return objSchema;
        } finally {
            seen.remove(clazz);
        }
    }

    private static void applyParam(AnnotatedElement element,
                            Map<String, Object> fieldSchema,
                            String fieldName,
                            List<String> required) {
        Param anno = element.getAnnotation(Param.class);
        if (anno != null && !anno.description().isEmpty()) {
            fieldSchema.put("description", anno.description());
        }
        if (anno == null || anno.required()) {
            required.add(fieldName);
        }
    }

}
