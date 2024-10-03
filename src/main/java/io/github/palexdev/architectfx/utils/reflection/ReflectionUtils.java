package io.github.palexdev.architectfx.utils.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.lang.model.SourceVersion;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.Tuple3;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

public class ReflectionUtils {

    // ================================================================================
    // Constructors
    // ================================================================================
    private ReflectionUtils() {
    }

    // ================================================================================
    // Static Methods
    // ================================================================================

    public static <T> T create(Class<?> klass, Object... args) {
        try {
            return Reflect.onClass(klass.getName(), DependencyManager.instance().loader())
                .create(args)
                .get();
        } catch (ReflectException ex) {
            Logger.error("Failed to create class {}:\n{}", klass.getName(), ex);
            return null;
        }
    }

    public static <T> T create(String className, Object... args) {
        try {
            Class<?> klass = ClassScanner.findClass(className);
            return create(klass, args);
        } catch (ClassNotFoundException | IllegalStateException ex) {
            Logger.error("Failed to create class {}:\n{}", className, ex);
            return null;
        }
    }

    public static <T> T invokeFactory(Object obj, Object... args) {
        if (obj instanceof String s) {
            return invokeFactory(s, args);
        }
        Logger.error("Expected factory as String but was: {}", Objects.toString(obj));
        return null;
    }

    public static <T> T invokeFactory(String factoryName, Object... args) {
        try {
            Tuple2<Class<?>, String> mInfo = ReflectionUtils.getMethodInfo(factoryName);
            if (mInfo.a() == null)
                throw new IllegalArgumentException("Factory class not defined");
            Logger.trace("Invoking factory {} with args {}", mInfo, args);
            return Reflect.onClass(mInfo.a().getName(), DependencyManager.instance().loader())
                .call(mInfo.b(), args)
                .get();
        } catch (Exception ex) {
            Logger.error("Failed to invoke factory {} because:\n{}", factoryName, ex);
            return null;
        }
    }

    public static Tuple3<Class<?>, String, Object> getFieldInfo(Object obj, boolean allowEnums) {
        if (obj instanceof String s) {
            try {
                if (!SourceVersion.isIdentifier(s) && !SourceVersion.isName(s))
                    return null;
                int lastDot = s.lastIndexOf('.');
                String sClass = (lastDot == -1) ? null : s.substring(0, lastDot);
                String sField = s.substring(lastDot + 1);

                Class<?> klass;
                if (sClass == null ||
                    (klass = ClassScanner.findClass(sClass)) == null ||
                    (klass.isEnum() && !allowEnums))
                    return Tuple3.of(null, sField, null);

                Object val = Reflect.onClass(klass).get(sField);
                return Tuple3.of(klass, sField, val);
            } catch (Exception ex) {
                Logger.error("Failed to retrieve field info\n{}", ex);
            }
        }
        return null;
    }

    public static Tuple2<Class<?>, String> getMethodInfo(Object obj) {
        if (obj instanceof String s) {
            try {
                if (!SourceVersion.isIdentifier(s) && !SourceVersion.isName(s)) return null;
                int lastDot = s.lastIndexOf('.');
                String sClass = (lastDot == -1) ? null : s.substring(0, lastDot);
                String sMethod = s.substring(lastDot + 1);
                Class<?> klass = null;
                if (sClass != null) klass = ClassScanner.findClass(sClass);
                return Tuple2.of(klass, sMethod);
            } catch (Exception ex) {
                Logger.error("Failed to retrieve method info\n{}", ex);
            }
        }
        return null;
    }

    public static void addToCollection(Object obj, String name, List<?> list) {
        // Retrieve collection via getter
        Collection<? super Object> collection;
        try {
            String getter = resolveGetter(name);
            Logger.debug("Attempting to retrieve collection {} via getter {}", name, getter);
            collection = Reflect.on(obj).call(getter).get();
            assert collection != null;
        } catch (AssertionError | ReflectException ex) {
            Logger.error("Failed to retrieve collection {} because:\n{}", name, ex);
            return;
        }

        Logger.trace("Adding values to retrieved collection...");
        collection.addAll(list);
    }

    public static void setProperty(Object obj, Property property) {
        String name = property.name();
        Object value = property.value();
        // Do it via setter
        try {
            String setter = resolveSetter(name);
            Logger.debug(
                "Attempting to set property of type {} via setter {} to {}",
                property.type(), setter, value
            );
            Reflect.on(obj).call(setter, value);
        } catch (ReflectException ex) {
            Logger.error("Failed to set {} value because:\n{}", property.type(), ex);
        }
    }

    public static String resolveGetter(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static String resolveSetter(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}