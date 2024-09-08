package io.github.palexdev.architectfx.utils;

import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CastUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private CastUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static <T> T as(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }
        throw new IllegalArgumentException("Expected type %s but found: %s".formatted(type, obj));
    }

    public static <T> List<T> asList(List<?> src, Class<T> type) {
        return src.stream()
            .map(type::cast)
            .toList();
    }

    public static <T> List<T> asList(Object obj, Class<T> type) {
        if (!(obj instanceof List<?>))
            throw new IllegalArgumentException("Expected list but found: %s".formatted(obj));
        return asList((List<?>) obj, type);
    }

    public static <M extends Map<K, V>, K, V> M asMap(Map<?, ?> map, Class<K> kType, Class<V> vType, Supplier<M> mapBuilder) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(
                kType::cast,
                vType::cast,
                (v, v2) -> v2,
                mapBuilder
            ));
    }

    public static <M extends Map<K, V>, K, V> M asMap(Object obj, Class<K> kType, Class<V> vType, Supplier<M> mapBuilder) {
        if (!(obj instanceof Map<?, ?>))
            throw new IllegalArgumentException("Expected map but found: %s".formatted(obj));
        return asMap(((Map<?, ?>) obj), kType, vType, mapBuilder);
    }

    public static SequencedMap<String, Object> asYamlMap(Object obj) {
        if (obj instanceof SequencedMap<?,?>) {
            return (SequencedMap<String, Object>) obj;
        }
        throw new IllegalArgumentException("Expected SequencedMap but found: %s".formatted(obj));
    }
}
