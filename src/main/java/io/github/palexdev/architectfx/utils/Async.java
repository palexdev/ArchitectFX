package io.github.palexdev.architectfx.utils;

import java.util.concurrent.*;

public class Async {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    //================================================================================
    // Constructors
    //================================================================================
    private Async() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Executes the given action in a [ExecutorService] which uses virtual threads.
    public static CompletableFuture<Void> run(Runnable action) {
        return CompletableFuture.runAsync(action, executor);
    }

    /// Executes the given action in a [ExecutorService] which uses virtual threads, and returns the result of the
    /// action once finished.
    public static <T> CompletableFuture<T> call(Callable<T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return action.call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        }, executor);
    }
}
