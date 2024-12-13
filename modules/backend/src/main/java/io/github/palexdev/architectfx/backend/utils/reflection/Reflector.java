package io.github.palexdev.architectfx.backend.utils.reflection;

import java.util.Optional;

import io.github.palexdev.architectfx.backend.enums.CollectionType;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

/// Utility class designed to specifically deal with reflection. This is not a singleton because some of its operations
/// depend on the [Scanner], which also deals with reflection, but it's more specific.
public class Reflector {
    //================================================================================
    // Properties
    //================================================================================
    private final Scanner scanner;

    //================================================================================
    // Constructors
    //================================================================================
    public Reflector(Scanner scanner) {
        this.scanner = scanner;
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Attempts at creating an instance of the given class with the given arguments.
    ///
    /// @return the created object or `null` if something went wrong
    /// @see Reflect
    public <T> T instantiate(Class<?> klass, Object... args) {
        try {

            return Reflect.onClass(klass)
                .create(args)
                .get();
        } catch (ReflectException ex) {
            Logger.error("Failed to create class {}:\n{}", klass.getName(), ex);
            return null;
        }
    }

    /// Given the name of a class, which can be simple or fully qualified, finds the related [Class] with
    /// [Scanner#findClass(String)] and delegates to [#instantiate(Class,Object...)].
    ///
    /// @return the created object or `null` if the creation failed or the class could not be found
    public <T> T instantiate(String className, Object... args) {
        try {
            Class<?> klass = scanner.findClass(className);
            return instantiate(klass, args);
        } catch (ClassNotFoundException ex) {
            Logger.error("Failed to create class {}:\n{}", className, ex);
            return null;
        }
    }

    /// Invokes a method with the given name and arguments on the given object.
    ///
    /// If the object is a [String] or a [Class] it assumes you want to invoke a static method. In the first case, the
    /// string is resolved to a class using [Scanner#findClass(String)].
    ///
    /// @return the result of the invoked method, wrapped in an [Optional] object
    public <T> Optional<T> invoke(Object target, String name, Object... args) {
        try {
            if (target == null)
                throw new IllegalArgumentException("No target given for method invocation");

            // Static call
            Class<?> klass = switch (target) {
                case String s -> scanner.findClass(s);
                case Class<?> c -> c;
                default -> null;
            };
            if (klass != null) {
                return Optional.ofNullable(
                    Reflect.onClass(klass)
                        .call(name, args)
                        .get()
                );
            }

            // Instance call
            return Optional.ofNullable(
                Reflect.on(target)
                    .call(name, args)
                    .get()
            );
        } catch (ReflectException | ClassNotFoundException ex) {
            Logger.error("Failed to invoke method {} because:\n{}", name, ex);
            return Optional.empty();
        }
    }

    /// Retrieves the value of a field with the given name from the given object.
    ///
    /// If the object is a [String], it is first resolved to a class using [Scanner#findClass(String)].
    ///
    /// Delegates to [Getter#read(Object, String)].
    public <T> T get(Object target, String name) {
        try {
            if (target == null)
                throw new IllegalArgumentException("No target given for field retrieval");

            // Static field, replace target with the found class
            if (target instanceof String s)
                target = scanner.findClass(s);

            // Instance field, accessor prioritized
            return Getter.read(target, name);
        } catch (ReflectException | ClassNotFoundException ex) {
            Logger.error("Failed to get field {} because:\n{}", name, ex);
            return null;
        }
    }

    /// Sets the value of a field with the given name on the given object.
    ///
    /// If the object is a [String], it is first resolved to a class using [Scanner#findClass(String)]
    ///
    /// Delegates to [Setter#write(Object, String, Object)].
    public void set(Object target, String name, Object value) {
        try {
            if (target == null)
                throw new IllegalArgumentException("No target given for field set");

            // Static field, replace target with found class
            if (target instanceof String s)
                target = scanner.findClass(s);

            // Instance field, accessor prioritized
            Setter.write(target, name, value);
        } catch (ReflectException | ClassNotFoundException ex) {
            Logger.error("Failed to set field {} because:\n{}", name, ex);
        }
    }

    /// Handles a collection with the given name on the given target object. The values to add are specified as a generic
    /// object. The handling is delegated to a [CollectionHandler], chosen according to the type by
    /// [CollectionHandler#handlerFor(CollectionType)].
    public void handleCollection(Object target, String name, CollectionType type, Object value, boolean clear) {
        try {
            if (target == null)
                throw new IllegalArgumentException("No target given for collection handling");

            CollectionHandler handler = CollectionHandler.handlerFor(type);

            // Static field, replace target with found class
            if (target instanceof String s)
                target = scanner.findClass(s);

            // Instance field
            handler.handle(target, name, value, clear);
        } catch (ReflectException | ClassNotFoundException ex) {
            Logger.error("Failed to handle collection {} because:\n{}", name, ex);
        }
    }

    /// Ensures that a field and a given object are compatible with the given type.
    ///
    /// - The field is extracted from the `target` with the given name, with direct access
    /// - Both the field's type and the given `value` must be compatible with the given `expectedType`
    ///
    /// @return for performance reasons (this may be called before executing other actions), in case of success,
    /// returns the retrieved field wrapped in a [Reflect] object
    /// @throws ReflectException if the field or the value are not compatible with `expectedType`
    public static Reflect checkTypes(Object target, String name, Object value, Class<?> expectedType) {
        if (!expectedType.isInstance(value))
            throw new ReflectException(
                "Expected target object to be of type %s but got %s"
                    .formatted(expectedType, value.getClass())
            );

        Reflect field = ((target instanceof Class<?> c) ?
            Reflect.onClass(c).field(name) :
            Reflect.on(target)).field(name);
        if (!expectedType.isAssignableFrom(field.type()))
            throw new ReflectException(
                "Expected source obj to be of type %s for name %s but got %s"
                    .formatted(name, expectedType, field.type())
            );

        return field;
    }
}
