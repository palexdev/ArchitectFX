package io.github.palexdev.architectfx.backend.utils.reflection;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.Function;

/// A collection of utilities specifically for arrays.
@SuppressWarnings({"unchecked", "RedundantCast"})
public class ArrayUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final Map<Class<?>, Function<Object, ?>> converters = Map.of(
        char.class, (o) -> ((char) o),
        boolean.class, o -> ((boolean) o),
        byte.class, o -> ((Number) o).byteValue(),
        short.class, o -> ((Number) o).shortValue(),
        int.class, o -> ((Number) o).intValue(),
        long.class, o -> ((Number) o).longValue(),
        float.class, o -> ((Number) o).floatValue(),
        double.class, o -> ((Number) o).doubleValue()
    );

    //================================================================================
    // Constructors
    //================================================================================
    private ArrayUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Given a type and an array of values, creates an array object using [Array#newInstance(Class, int)] and casts
    /// populates it by casting each value to the given type using [Array#set(Object, int, Object)].
    ///
    /// Arrays in Java are special and troublesome. They do not have type erasure and an array is still a single object.
    public static <T> T createArray(Class<?> componentType, Object... args) {
        if (args.length == 0) return null;
        if (componentType == null)
            throw new IllegalArgumentException("Cannot create array without knowing its component type");

        Function<Object, ?> converter = converters.getOrDefault(componentType, componentType::cast);
        Object arr = Array.newInstance(componentType, args.length);
        for (int i = 0; i < args.length; i++) {
            Array.set(arr, i, converter.apply(args[i]));
        }
        return (T) arr;
    }
}
