package io.github.palexdev.architectfx.utils;

import java.util.function.Function;
import java.util.function.Supplier;

/// Simple extension of [Supplier] which implements a chaining mechanism similar to [Function#andThen(Function)].
public interface ChainSupplier<T> extends Supplier<T> {
    default ChainSupplier<T> andThen(Function<T, T> other) {
        return () -> other.apply(get());
    }
}
