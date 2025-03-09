package io.github.palexdev.architectfx.backend.utils.reflection;

import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

/// API to retrieve a certain field/value from a target object (can be a class if it's static) given its name.
///
/// There are two internal concrete implementations which can extract the field/value by direct access or through the
/// corresponding getter method (this is given by [#getterFor(String)]).
///
/// @see #read(Object, String)
public sealed interface Getter permits Getter.Direct, Getter.Accessor {

    /// Retrieves a field/value for the given name from the given target object.
    <T> T get(Object target, String name);

    /// Tries to retrieve a field for the given name from the given object first by invoking the getter, and in case of
    /// failure with direct access.
    ///
    /// Delegates to [Accessor#get(Object, String)] and [Direct#get(Object, String)].
    /// If the object is a class, automatically delegates to [#get(Class, String)].
    ///
    /// @throws ReflectException if both strategies failed
    static <T> T read(Object target, String name) {
        try {
            return accessor().get(target, name);
        } catch (Exception ex) {
            Logger.trace(ex);
        }

        try {
            return direct().get(target, name);
        } catch (Exception ex) {
            Logger.trace(ex);
        }

        throw new ReflectException("Read access to field failed with both accessor and direct approaches");
    }

    /// Given the name of a field, returns a getter according to Java's conventions, so: 'get' + name with the first
    /// letter of name being capital.
    static String getterFor(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    static Getter direct() {
        return Direct.INSTANCE;
    }

    static Getter accessor() {
        return Accessor.INSTANCE;
    }

    /// Implementation of [Getter], access fields directly, uses [Reflect#get(String)].
    final class Direct implements Getter {
        private static final Direct INSTANCE = new Direct();

        private Direct() {}

        @Override
        public <T> T get(Object target, String name) {
            Reflect reflect = (target instanceof Class<?> c) ?
                Reflect.onClass(c) :
                Reflect.on(target);
            return reflect.get(name);
        }
    }

    /// Implementation of [Getter], access fields through a getter method, uses [Reflect#call(String)].
    ///
    /// @see Getter#getterFor(String)
    final class Accessor implements Getter {
        private static final Accessor INSTANCE = new Accessor();

        private Accessor() {}

        @Override
        public <T> T get(Object target, String name) {
            String getter = getterFor(name);
            Reflect reflect = (target instanceof Class<?> c) ?
                Reflect.onClass(c) :
                Reflect.on(target);
            return reflect.call(getter).get();
        }
    }
}
