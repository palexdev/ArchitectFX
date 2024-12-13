/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.architectfx.backend.model.types;


import java.net.URI;

import io.github.palexdev.architectfx.backend.enums.CollectionType;
import io.github.palexdev.architectfx.backend.enums.Keyword;
import io.github.palexdev.architectfx.backend.enums.ValueType;
import io.github.palexdev.architectfx.backend.model.UIObj;

/// Represents any possible kind of value (see [ValueType]) in the UI document. Effectively, this is an intermediate
/// product which allows and simplify the load and management of the UI. The document's text is parsed into this
/// (to be precise in the appropriate implementation/subclass), which is then used by reflection to initialize the UI tree.
///
/// Wraps two pieces of information:
/// 1) The parsed value as a generic `T` type
/// 2) An enum constant of type [ValueType] which given a clue on what kind of value we're dealing with
public abstract class Value<T> {
    //================================================================================
    // Properties
    //================================================================================
    protected final ValueType type;
    protected T value;

    //================================================================================
    // Constructors
    //================================================================================
    public Value(ValueType type, T value) {
        this.type = type;
        this.value = value;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        return "Value{" +
               "type=" + type +
               ", value=" + value +
               '}';
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public ValueType getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    //================================================================================
    // Inner Classes
    //================================================================================

    /// Implementation of [Value] which holds values of type [Boolean].
    public static class BooleanValue extends Value<Boolean> {
        public BooleanValue(Boolean value) {
            super(ValueType.BOOLEAN, value);
        }
    }

    /// Implementation of [Value] which holds values of type [CharValue].
    public static class CharValue extends Value<Character> {
        public CharValue(Character value) {
            super(ValueType.CHAR, value);
        }
    }

    /// Implementation of [Value] which holds values of type [String].
    public static class StringValue extends Value<String> {
        public StringValue(String value) {
            super(ValueType.STRING, value);
        }
    }

    /// Implementation of [Value] which holds values of type [Number] (yes, any number).
    public static class NumberValue extends Value<Number> {
        public NumberValue(Number value) {
            super(ValueType.NUMBER, value);
        }
    }

    /// Implementation of [Value] which holds array values.
    ///
    /// Arrays in Java are a bit problematic. There's no type erasure for them, which means we must get the type right
    /// to avoid any error when initializing the tree. If we look at the _JUI_ language, after some tests, I decided to
    /// make the grammar enforce a certain syntax: `Type[values...]`. Before the values, one must specify which is the
    /// component type of the array.
    /// For such reason, this wraps an additional piece of information, which is in fact the component type of the array.
    /// It is stored as a [String], which eventually going to be resolved to a [Class] via reflection.
    public static class ArrayValue extends Value<Value<?>[]> {
        private final String componentType;

        public ArrayValue(Value<?>[] value, String componentType) {
            super(ValueType.ARRAY, value);
            this.componentType = componentType;
        }

        /// @return the component type/class of the wrapped array as a [String]
        public String getComponentType() {
            return componentType;
        }
    }

    /// Implementation of [Value] which holds collection values. Allowed collections are specified by the[CollectionType]
    /// enumeration.
    ///
    /// To be precise, the wrapped value is not a collection but an array of [Value] objects. During the load/init process,
    /// every [Value] in the array is converted to a generic [Object] and the appropriate collection is built by
    /// [CollectionType#create(Object...)].
    public static class CollectionValue extends Value<Value<?>[]> {
        private final CollectionType collectionType;

        public CollectionValue(Value<?>[] value, CollectionType collectionType) {
            super(ValueType.COLLECTION, value);
            this.collectionType = collectionType;
        }

        /// @return the type of collection to build from this [Value]
        /// @see CollectionType
        public CollectionType getCollectionType() {
            return collectionType;
        }
    }

    /// Implementation of [Value] which holds values of type [UIObj].
    public static class UIObjValue extends Value<UIObj> {
        public UIObjValue(UIObj value) {
            super(ValueType.OBJ, value);
        }
    }

    /// Implementation of [Value] which holds values of type [MethodsChain].
    public static class MethodsValue extends Value<MethodsChain> {
        public MethodsValue(MethodsChain value) {
            super(ValueType.METHODS, value);
        }
    }

    /// Implementation of [Value] which holds values of type [FieldRef].
    public static class FieldValue extends Value<FieldRef> {
        public FieldValue(FieldRef value) {
            super(ValueType.FIELD, value);
        }
    }

    /// Implementation of [Value] which holds values of type [FieldRef].
    ///
    /// Keywords truly are special values, each kind can be interpreted differently by the loading process. Some of them
    /// may even transport some kind of payload.
    ///
    /// For example, the [Keyword#INJECTION] keyword is used to tell the system a value should be injected given a name
    /// as the payload.
    ///
    /// The harsh thing in this is that the payload is a generic array of objects, and since each keyword may carry a
    /// different number and kind of values, it's responsibility of the loading process to handle the payload correctly.
    public static class KeywordValue extends Value<Keyword> {
        private Object[] payload;

        public KeywordValue(Keyword value, Object[] payload) {
            super(ValueType.KEYWORD, value);
            this.payload = payload;
        }

        public Object[] getPayload() {
            return payload;
        }

        public void setPayload(Object[] payload) {
            this.payload = payload;
        }
    }

    /// Implementation of [Value] which holds values of type [String] specifically to be used to indicate an external
    /// resource. In fact, the wrapped string must be compliant with the syntax rules specified by [URI].
    ///
    /// During the load process, the string may be converted to a [URI] by calling [#toURI()], the converted value is
    /// cached until the src string changes (if one calls [#setValue(String)]).
    public static class URLValue extends Value<String> {
        private URI uri;

        public URLValue(String value) {
            super(ValueType.URL, value);
        }

        @Override
        public void setValue(String value) {
            uri = null;
            super.setValue(value);
        }

        /// Convenience method to convert [#getValue()] to a [URI]. The converted object is also cached until
        /// [#setValue(String)] is called.
        public URI toURI() {
            if (uri == null) {
                uri = URI.create(value);
            }
            return uri;
        }
    }
}
