package io.github.palexdev.architectfx.backend.enums;

import java.util.Collection;
import java.util.Map;

import io.github.palexdev.architectfx.backend.yaml.Keyword;
import org.tinylog.Logger;

/// Widely used enumeration which allows the system to determine on which type of data it's working on and often used
/// to decide how to treat such data.
public enum Type {
    /// This constant is reserved for all system tags. Strings that start with '.'
    METADATA,

    /// Constant used to identify primitive data types (int, double,...)
    PRIMITIVE,

    /// Constant used to identify wrapper data types (Integer, Double,...)
    WRAPPER,

    /// Constant used to identify strings
    STRING,

    /// Constant used to identify data of type [Enum]
    ENUM,

    /// Constant used to identify 'complex' data. Complex mostly means other objects, "composite" data
    COMPLEX,

    /// Constant used to identify collections, mostly lists
    COLLECTION,

    /// This constant is reserved for system keywords, [Keyword]
    KEYWORD,

    /// Fallback constant used when knowing the type is unnecessary or not possible (for whatever reason)
    UNKNOWN,
    ;

    private static final Map<Class<?>, Class<?>> wrappers = Map.of(
        Boolean.class, boolean.class,
        Character.class, char.class,
        Byte.class, byte.class,
        Short.class, short.class,
        Integer.class, int.class,
        Long.class, long.class,
        Float.class, float.class,
        Double.class, double.class
    );

    /// Delegates to [#getType(Class)].
    public static Type getType(Object obj) {
        return getType(obj.getClass());
    }

    /// By analyzing the given type/class, returns the appropriate enumeration.
    ///
    /// If none is suitable, returns [#UNKNOWN].
    public static Type getType(Class<?> klass) {
        return switch (klass) {
            case Class<?> k when k.isPrimitive() -> PRIMITIVE;
            case Class<?> k when wrappers.containsKey(k) -> WRAPPER;
            case Class<?> k when k == String.class -> STRING;
            case Class<?> k when k.isEnum() -> ENUM;
            case Class<?> k when Map.class.isAssignableFrom(k) -> COMPLEX;
            case Class<?> k when Collection.class.isAssignableFrom(k) -> COLLECTION;
            case Class<?> k when k == Keyword.class -> KEYWORD;
            default -> {
                Logger.warn("Could not determine type for class: " + klass);
                yield UNKNOWN;
            }
        };
    }

    /// Convenience method to check whether a String is system metadata.
    ///
    /// Simply checks if it starts with '.'.
    ///
    /// This decision was made because the dot is one of the few characters which is illegal for Java identifiers, but
    /// legal in YAML.
    public static boolean isMetadata(String val) {
        return val.startsWith(".");
    }

    /// @return the given class' corresponding primitive time, or `null` if that is not a wrapper type
    public static Class<?> primitiveOf(Class<?> klass) {
        return wrappers.get(klass);
    }
}
