package io.github.palexdev.architectfx.enums;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;

import io.github.palexdev.architectfx.utils.ClassScanner;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.asEnumClass;

@SuppressWarnings({"rawtypes","unchecked"})
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
    private static final Map<String, Class<? extends Enum>> enumsCache = new HashMap<>();

    public boolean isPrimitiveOrWrapperOrString() {
        return this == PRIMITIVE || this == WRAPPER || this == STRING;
    }

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
            default -> {
                Logger.warn("Could not determine type for class: " + klass);
                yield UNKNOWN;
            }
        };
    }

    public static boolean isMetadata(String val) {
        return val.startsWith(".");
    }

    public static Enum<?> isEnum(Object obj) {
        if (obj instanceof String s) {
            try {
                int lastDot = s.lastIndexOf('.');
                if (lastDot == -1) return null;

                Class<? extends Enum> klass;
                String sClass = s.substring(0, lastDot);
                String sValue = s.substring(lastDot + 1);
                if (!enumsCache.containsKey(sClass)) {
                    if (!SourceVersion.isIdentifier(sClass) && !SourceVersion.isName(sClass)) {
                        Logger.trace("String {} is not a valid class identifier", sClass);
                        return null;
                    }
                    klass = asEnumClass(ClassScanner.findClass(sClass));
                    if (!klass.isEnum()) {
                        Logger.trace("Class {} is not an enum", klass);
                        return null;
                    }
                    enumsCache.put(sClass, klass);
                } else {
                    klass = enumsCache.get(sClass);
                }
                return Enum.valueOf(klass, sValue);
            } catch (Exception ex) {
                Logger.error("Failed to parse enum from string {}\n{}", s, ex);
            }
        }
        return null;
    }
}
