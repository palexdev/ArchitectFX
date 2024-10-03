package io.github.palexdev.architectfx.enums;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.github.palexdev.architectfx.yaml.Keyword;
import org.tinylog.Logger;

public enum Type {
    METADATA,
    PRIMITIVE,
    WRAPPER,
    STRING,
    ENUM,
    COMPLEX,
    COLLECTION,
    KEYWORD,
    UNKNOWN,
    ;

    private static final Set<Class<?>> wrappers = Set.of(
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class
    );

    public static Type getType(Object obj) {
        return getType(obj.getClass());
    }

    public static Type getType(Class<?> klass) {
        return switch (klass) {
            case Class<?> k when k.isPrimitive() -> PRIMITIVE;
            case Class<?> k when wrappers.contains(k) -> WRAPPER;
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

    public static boolean isMetadata(String val) {
        return val.startsWith(".");
    }
}
