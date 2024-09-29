package io.github.palexdev.architectfx.enums;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.github.palexdev.architectfx.utils.ClassScanner;
import org.tinylog.Logger;

public enum Type {
    METADATA,
    PRIMITIVE,
    WRAPPER,
    STRING,
    ENUM,
    COMPLEX,
    COLLECTION,
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

    public boolean isPrimitiveOrWrapperOrString() {
        return this == PRIMITIVE || this == WRAPPER || this == STRING;
    }

    public static Type getType(Object obj) {
        // Handle String values
        if (obj instanceof String s) {
            String[] split = s.split("\\.");
            if (split.length < 2) return STRING;
            try {
                return getType(ClassScanner.findClass(split[0]));
            } catch (ClassNotFoundException ex) {
                Logger.error(
                    "Failed to determine type for {} because class {} was not found",
                    s, split[0]
                );
            }

        }
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
