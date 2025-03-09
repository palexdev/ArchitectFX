package io.github.palexdev.architectfx.backend.utils.reflection;

import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

/// API to set a certain field on a target object (can be a class if it's static) given its name and the target value.
///
/// There are two internal concrete implementations which can set the field by direct access or through the
/// corresponding setter method (this is given by [#setterFor(String)]).
///
/// @see #write(Object, String, Object)
public sealed interface Setter permits Setter.Direct, Setter.Accessor {

    /// Sets the field with the given name on the given object to the given value.
    <T> T set(Object target, String name, Object value);

    /// Tries to set a field with the given name on the given object first by invoking the setter, and in case of
    /// failure with direct access.
    ///
    /// Delegates to [Accessor#set(Object, String, Object)] and [Direct#set(Object, String, Object)].
    /// If the object is a class, automatically delegates to [#set(Class, String, Object)].
    ///
    /// @throws ReflectException if both strategies failed
    static <T> T write(Object target, String name, Object value) {
        try {
            return accessor().set(target, name, value);
        } catch (Exception ex) {
            Logger.trace(ex);
        }

        try {
            return direct().set(target, name, value);
        } catch (Exception ex) {
            Logger.trace(ex);
        }

        throw new ReflectException("Write access to field failed with both accessor and direct approaches");
    }

    /// Given the name of a field, returns a setter according to Java's conventions, so: 'set' + name with the first
    /// letter of name being capital.
    static String setterFor(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    static Setter direct() {
        return Direct.INSTANCE;
    }

    static Setter accessor() {
        return Accessor.INSTANCE;
    }

    /// Implementation of [Setter], access fields directly, uses [Reflect#set(String, Object)].
    final class Direct implements Setter {
        private static final Direct INSTANCE = new Direct();

        private Direct() {}

        @Override
        public <T> T set(Object target, String name, Object value) {
            Reflect reflect = (target instanceof Class<?> c) ?
                Reflect.onClass(c) :
                Reflect.on(target);
            return reflect.set(name, value).get();
        }
    }

    /// Implementation of [Setter], access fields through a getter method, uses [Reflect#call(String)].
    ///
    /// @see Setter#setterFor(String)
    final class Accessor implements Setter {
        private static final Accessor INSTANCE = new Accessor();

        private Accessor() {}

        @Override
        public <T> T set(Object target, String name, Object value) {
            String setter = setterFor(name);
            Reflect reflect = (target instanceof Class<?> c) ?
                Reflect.onClass(c) :
                Reflect.on(target);
            return reflect.call(setter, value).get();
        }
    }
}
