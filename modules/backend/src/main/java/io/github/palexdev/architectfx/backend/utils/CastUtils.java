package io.github.palexdev.architectfx.backend.utils;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes","unchecked"})
public class CastUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private CastUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Casts the given object to a generic `T` type.
    public static <T> T unchecked(Object obj) {
        return ((T) obj);
    }

    /// Casts the given object to the given type.
    public static <T> T unchecked(Object obj, Class<T> type) {
        return type.cast(obj);
    }

    /// Casts the given object to the given type.
    ///
    /// @throws IllegalArgumentException if the object is not of the given type
    public static <T> T as(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return unchecked(obj, type);
        }
        throw new IllegalArgumentException("Expected type %s but found: %s".formatted(type, obj));
    }

    /// Casts the given type to a generic enum type.
    public static Class<? extends Enum> asEnumClass(Class<?> klass) {
        return (Class<? extends Enum>) klass;
    }

    /// Casts each element of the given list to the given type and then collects the results in a new list.
    public static <T> List<T> asList(List<?> src, Class<T> type) {
        return src.stream()
            .map(type::cast)
            .toList();
    }


    /// Casts the given object to `List<Object>`.
    ///
    /// @throws IllegalArgumentException if the given object is not a list
    public static List<Object> asGenericList(Object obj) {
        if (!(obj instanceof List<?>))
            throw new IllegalArgumentException("Expected list but found: %s".formatted(obj));
        return (List<Object>) obj;
    }

    /// If the given object is a list calls [#asList(List,Class)].
    ///
    /// @throws IllegalArgumentException if the given object is not a list
    public static <T> List<T> asList(Object obj, Class<T> type) {
        if (!(obj instanceof List<?>))
            throw new IllegalArgumentException("Expected list but found: %s".formatted(obj));
        return asList((List<?>) obj, type);
    }

    /// Casts the given object to a generic map.
    public static Map<Object, Object> asGenericMap(Object obj) {
        if (!(obj instanceof Map<?, ?>))
            throw new IllegalArgumentException("Expected map but found: %s".formatted(obj));
        return (Map<Object, Object>) obj;
    }
}
