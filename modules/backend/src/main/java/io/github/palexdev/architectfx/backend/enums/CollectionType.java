package io.github.palexdev.architectfx.backend.enums;


import java.util.*;

/// This enumeration specifies the types of collections supported by _JUI_ and each constant defines how such type is built.
@SuppressWarnings("unchecked")
public enum CollectionType {
    LIST(List.class) {
        /// Uses [Arrays#asList(Object\[\])].
        @Override
        public List<?> create(Object... items) {
            return Arrays.asList(items);
        }
    },
    /// Uses a [LinkedHashMap].
    ///
    /// For this to work properly, the number of elements specified in the array must be a multiple of 2.
    /// Basically, this works similarly as [Map#of()], but uses a vararg to allow way more than 10 pairs.
    /// Elements in the array will be treated as pairs, e.g: Entry {items[0], items[1]}.
    MAP(Map.class) {
        @Override
        public Map<?, ?> create(Object... items) {
            if (items.length % 2 != 0)
                throw new IllegalArgumentException("Items length must be even to create a map");
            Map<Object, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < items.length; i += 2) {
                map.put(items[i], items[i + 1]);
            }
            return map;
        }
    },
    /// Uses a [LinkedHashSet],
    SET(Set.class) {
        @Override
        public Set<?> create(Object... items) {
            Set<Object> set = new LinkedHashSet<>();
            Collections.addAll(set, items);
            return set;
        }
    },
    ;

    private final Class<?> klass;

    CollectionType(Class<?> klass) {this.klass = klass;}

    /// Each type knows how to create the corresponding collection object, given an array of elements.
    public abstract <T> T create(Object... items);

    /// The collection's type is expressed both by the enum constant and the class returned by this method.
    public Class<?> klass() {return klass;}

    /// Converts a literal from _JUI_ to the appropriate collection type:
    /// - 'listOf': [#LIST]
    /// - 'mapOf': [#MAP]
    /// - 'setOf': [#SET]
    public static CollectionType fromString(String s) {
        return switch (s.trim()) {
            case "listOf" -> LIST;
            case "mapOf" -> MAP;
            case "setOf" -> SET;
            default -> null;
        };
    }
}
