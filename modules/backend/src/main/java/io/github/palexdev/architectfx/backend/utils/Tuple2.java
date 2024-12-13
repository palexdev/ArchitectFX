package io.github.palexdev.architectfx.backend.utils;

/// Simple wrapper for two types of values.
public record Tuple2<A, B>(A a, B b) {
    //================================================================================
    // Static Methods
    //================================================================================
    public static <A, B> Tuple2<A, B> of(A a, B b) {
        return new Tuple2<>(a, b);
    }
}
