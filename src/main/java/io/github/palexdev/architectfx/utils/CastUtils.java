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

import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CastUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private CastUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static <T> T as(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return type.cast(obj);
        }
        throw new IllegalArgumentException("Expected type %s but found: %s".formatted(type, obj));
    }

    public static Class<? extends Enum> asEnumClass(Class<?> klass) {
        return (Class<? extends Enum>) klass;
    }

    public static <T> List<T> asList(List<?> src, Class<T> type) {
        return src.stream()
            .map(type::cast)
            .toList();
    }

    public static List<Object> asGenericList(Object obj) {
        return asList(obj, Object.class);
    }

    public static <T> List<T> asList(Object obj, Class<T> type) {
        if (!(obj instanceof List<?>))
            throw new IllegalArgumentException("Expected list but found: %s".formatted(obj));
        return asList((List<?>) obj, type);
    }

    public static <M extends Map<K, V>, K, V> M asMap(Map<?, ?> map, Class<K> kType, Class<V> vType, Supplier<M> mapBuilder) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(
                kType::cast,
                vType::cast,
                (v, v2) -> v2,
                mapBuilder
            ));
    }

    public static <M extends Map<K, V>, K, V> M asMap(Object obj, Class<K> kType, Class<V> vType, Supplier<M> mapBuilder) {
        if (!(obj instanceof Map<?, ?>))
            throw new IllegalArgumentException("Expected map but found: %s".formatted(obj));
        return asMap(((Map<?, ?>) obj), kType, vType, mapBuilder);
    }

    public static SequencedMap<String, Object> asYamlMap(Object obj) {
        if (obj instanceof SequencedMap<?, ?>) {
            return (SequencedMap<String, Object>) obj;
        }
        throw new IllegalArgumentException("Expected SequencedMap but found: %s".formatted(obj));
    }
}
