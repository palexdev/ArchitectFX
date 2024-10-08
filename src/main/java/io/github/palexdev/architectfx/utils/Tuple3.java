package io.github.palexdev.architectfx.utils;

/// A simple record, wrapper for three types of data, `A`, `B` and `C`.
public record Tuple3<A, B, C>(A a, B b, C c) {

    //================================================================================
    // Static Methods
    //================================================================================
    public static <A, B, C> Tuple3<A, B, C> of(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }
}
