package io.github.palexdev.architectfx.backend.utils.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.lang.model.SourceVersion;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.deps.DynamicClassLoader;
import io.github.palexdev.architectfx.backend.model.Property;
import io.github.palexdev.architectfx.backend.utils.Tuple2;
import io.github.palexdev.architectfx.backend.utils.Tuple3;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

/// Utility class designed to specifically deal with reflection. This is not a singleton because some of its operations
/// depend on the [DynamicClassLoader] (given by the [DependencyManager]) and the [ClassScanner].
public class Reflector {
    //================================================================================
    // Properties
    //================================================================================
    private DependencyManager dm;
    private ClassScanner scanner;

    // ================================================================================
    // Constructors
    // ================================================================================
    public Reflector(DependencyManager dm, ClassScanner scanner) {
        this.dm = dm;
        this.scanner = scanner;
    }

    // ================================================================================
    // Static Methods
    // ================================================================================

    /// Attempts at creating an instance of the given class with the given arguments.
    ///
    /// @return the created object or `null` if something went wrong
    /// @see Reflect
    public <T> T create(Class<?> klass, Object... args) {
        try {
            return Reflect.onClass(klass)
                .create(args)
                .get();
        } catch (ReflectException ex) {
            Logger.error("Failed to create class {}:\n{}", klass.getName(), ex);
            return null;
        }
    }

    /// Given the name of a class, which can be simple or fully qualified, find the related `Class` with
    /// [ClassScanner#findClass(String)] and delegates to [#create(Class,Object...)].
    ///
    /// @return the created object or `null` if the creation failed or the class could not be found
    public <T> T create(String className, Object... args) {
        try {
            Class<?> klass = scanner.findClass(className);
            return create(klass, args);
        } catch (ClassNotFoundException | IllegalStateException ex) {
            Logger.error("Failed to create class {}:\n{}", className, ex);
            return null;
        }
    }

    /// This method is specifically to create objects through factories/builders.
    ///
    /// For simplicity, the `factoryName` is expected to be in these formats: `package.FactoryClass.staticMethod` or
    /// `FactoryClass.staticMethod`.
    ///
    /// If the class is found, proceeds to invoke the specified static method with the given args.
    ///
    /// @return the object created by the factory's method, or `null` if something went wrong
    /// @see Reflect
    /// @see Reflect#onClass(String, ClassLoader)
    public <T> T invokeFactory(String factoryName, Object... args) {
        try {
            Tuple2<Class<?>, String> mInfo = getMethodInfo(factoryName);
            if (mInfo.a() == null)
                throw new IllegalArgumentException("Factory class not defined");
            Logger.trace("Invoking factory {} with args {}", mInfo, args);
            return Reflect.onClass(mInfo.a().getName(), dm.loader())
                .call(mInfo.b(), args)
                .get();
        } catch (Exception ex) {
            Logger.error("Failed to invoke factory {} because:\n{}", factoryName, ex);
            return null;
        }
    }

    /// Expects the given object to be a string and delegates to [#invokeFactory(String,Object...)].
    ///
    /// @return the created object or `null` if the creation failed or the `factory` is not a string
    public <T> T invokeFactory(Object factoryName, Object... args) {
        if (factoryName instanceof String s) {
            return invokeFactory(s, args);
        }
        Logger.error("Expected factory as String but was: {}", Objects.toString(factoryName));
        return null;
    }

    /// This method gathers information about a _static field_.
    /// The given `obj` parameter is expected to be a string.
    ///
    /// Basically two things can happen:
    ///  1) The string is either of these two formats: `package.Class.staticField`, `Class.staticField`.
    /// In such cases, if the class is found by the [ClassScanner], this will return a tuple which specifies in order:
    /// the class which "owns" the field, the name of the field and the field's value. Otherwise, falls in case 2.
    ///  2) Assumes that it is not a static field, returns a tuple with `null` "owner" class, the extracted field
    /// name and `null` value.
    ///
    /// To my surprise, enum constants are considered static fields too. The `allowEnums` parameter allows to
    /// regulate such behavior and eventually fall in case 2.
    ///
    /// Note: yes, this is a naive approach for instance fields because we don't really need info about them
    public Tuple3<Class<?>, String, Object> getFieldInfo(Object obj, boolean allowEnums) {
        if (obj instanceof String s) {
            try {
                if (!SourceVersion.isIdentifier(s) && !SourceVersion.isName(s))
                    return null;
                int lastDot = s.lastIndexOf('.');
                String sClass = (lastDot == -1) ? null : s.substring(0, lastDot);
                String sField = s.substring(lastDot + 1);

                Class<?> klass;
                if (sClass == null ||
                    (klass = scanner.findClass(sClass)) == null ||
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

    /// This method gathers information about a _static method_.
    /// The given `obj` parameter is expected to be a string.
    ///
    /// Basically two things can happen:
    ///  1) The string is either of these two formats: `package.Class.staticMethod`, `Class.staticMethod`.
    /// In such cases, if the class is found by the [ClassScanner], this will return a tuple which specifies in order:
    /// the class which "owns" the method and the method's value. Otherwise, falls in case 2.
    ///  2) Assumes that it is not a static method, returns a tuple with `null` "owner" class and the extracted method name.
    public Tuple2<Class<?>, String> getMethodInfo(Object obj) {
        if (obj instanceof String s) {
            try {
                if (!SourceVersion.isIdentifier(s) && !SourceVersion.isName(s)) return null;
                int lastDot = s.lastIndexOf('.');
                String sClass = (lastDot == -1) ? null : s.substring(0, lastDot);
                String sMethod = s.substring(lastDot + 1);
                Class<?> klass = null;
                if (sClass != null) klass = scanner.findClass(sClass);
                return Tuple2.of(klass, sMethod);
            } catch (Exception ex) {
                Logger.error("Failed to retrieve method info\n{}", ex);
            }
        }
        return null;
    }

    /// Sets to `null` the references to the [DependencyManager] and the [ClassScanner].
    public void dispose() {
        dm = null;
        scanner = null;
    }

    //================================================================================
    // Static Methods
    //================================================================================

    /// This method is responsible for adding the elements in the given `list` to a collection in the given `obj` which
    /// has the given `name`.
    ///
    /// In other words, this is a naive approach to populate collections with reflection.
    /// A certain object is expected to have a collection with a certain name and a **getter** in the form:
    /// `get + name` (with the first letter of name being capitalized).
    ///
    /// This method retrieves the collection with the resolved getter and adds all elements to it.
    ///
    /// @see Reflect
    /// @see #resolveGetter(String)
    public static void addToCollection(Object obj, String name, List<?> list) {
        // Retrieve collection via getter
        Collection<? super Object> collection;
        try {
            String getter = resolveGetter(name);
            Logger.debug("Attempting to retrieve collection {} via getter {}", name, getter);
            collection = Reflect.on(obj).call(getter).get();
            if (collection == null)
                throw new IllegalArgumentException(
                    "Could not retrieve collection for name %s via getter %s".formatted(name, getter)
                );
        } catch (IllegalArgumentException | ReflectException ex) {
            Logger.error("Failed to retrieve collection {} because:\n{}", name, ex);
            return;
        }

        Logger.trace("Adding values to retrieved collection...");
        collection.addAll(list);
    }

    /// This method is responsible for setting the given `property` in the given `obj`.
    ///
    /// It's a naive approach since it expects that the object offers a setter in the form: `set + property.name`
    /// (with the first letter of the name being capitalized)
    ///
    /// @see Reflect
    /// @see #resolveSetter(String)
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

    /// Naive approach to generate a `getter` name from a `field` name.
    ///
    /// Simply does: `"get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)`
    public static String resolveGetter(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /// Naive approach to generate a `setter` name from a `field` name.
    ///
    /// Simply does: `"set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)`
    public static String resolveSetter(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}