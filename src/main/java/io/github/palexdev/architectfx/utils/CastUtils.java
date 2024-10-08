/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/MaterialFX)
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

package io.github.palexdev.architectfx.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/// Utility class which serves mainly two purposes:
///  1) Offers common cast operations that are needed by the deserialization process
///  2) Hiding Java warnings because I know what I'm doing...right?
@SuppressWarnings({"rawtypes", "unchecked"})
public class CastUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private CastUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Casts the given object to the given type.
    ///
    /// @throws IllegalArgumentException if the object is not of the given type
    public static <T> T as(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }
        throw new IllegalArgumentException("Expected type %s but found: %s".formatted(type, obj));
    }

    /// Casts the given type to a generic enum type.
    public static Class<? extends Enum> asEnumClass(Class<?> klass) {
        return (Class<? extends Enum>) klass;
    }

    /// Casts each element of the given list to the given type and then collects the results in a new list.
    public static <T> List<T> asList(List<?> src, Class<T> type) {
        return src.stream()
            .map(type::cast)
            .toList();
    }


    /// Casts the given object to `List<Object>`.
    ///
    /// @throws IllegalArgumentException if the given object is not a list
    public static List<Object> asGenericList(Object obj) {
        if (!(obj instanceof List<?>))
            throw new IllegalArgumentException("Expected list but found: %s".formatted(obj));
        return (List<Object>) obj;
    }

    /// If the given object is a list calls [#asList(List,Class)].
    ///
    /// @throws IllegalArgumentException if the given object is not a list
    public static <T> List<T> asList(Object obj, Class<T> type) {
        if (!(obj instanceof List<?>))
            throw new IllegalArgumentException("Expected list but found: %s".formatted(obj));
        return asList((List<?>) obj, type);
    }

    /// Creates a new map by casting every key and value in the given map respectively to: `kType` and `vType`.
    /// The `mapBuilder` parameter lets you decide what map type to create.
    public static <M extends Map<K, V>, K, V> M asMap(Map<?, ?> map, Class<K> kType, Class<V> vType, Supplier<M> mapBuilder) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(
                kType::cast,
                vType::cast,
                (v, v2) -> v2,
                mapBuilder
            ));
    }

    /// If the given object is a map, calls [#asMap(Map,Class,Class,Supplier)].
    ///
    /// @throws IllegalArgumentException if the given object is not a map
    public static <M extends Map<K, V>, K, V> M asMap(Object obj, Class<K> kType, Class<V> vType, Supplier<M> mapBuilder) {
        if (!(obj instanceof Map<?, ?>))
            throw new IllegalArgumentException("Expected map but found: %s".formatted(obj));
        return asMap(((Map<?, ?>) obj), kType, vType, mapBuilder);
    }

    /// `SnakeYaml` uses maps of type [LinkedHashMap], this method casts the given object to a generic
    /// [SequencedMap] of type `SequencedMap<String, Object>`.
    public static SequencedMap<String, Object> asYamlMap(Object obj) {
        if (!(obj instanceof SequencedMap<?, ?>)) {
            throw new IllegalArgumentException("Expected SequencedMap but found: %s".formatted(obj));
        }
        return (SequencedMap<String, Object>) obj;
    }
}
