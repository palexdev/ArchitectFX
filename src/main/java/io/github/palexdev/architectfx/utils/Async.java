package io.github.palexdev.architectfx.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Async {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    public static final CompletableFuture<Object> EMPTY_FUTURE = CompletableFuture.completedFuture(null);

    //================================================================================
    // Constructors
    //================================================================================
    private Async() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Executes the given action in an [ExecutorService] which uses virtual threads.
    public static CompletableFuture<Void> run(Runnable action) {
        return CompletableFuture.runAsync(action, executor);
    }

    /// Executes the given action in an [ExecutorService] which uses virtual threads and returns the result of the
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

    /// Convenience method to wrap a [Callable] in a [CompletableFuture]. The action is not run asynchronously!
    public static <T> CompletableFuture<T> wrap(Callable<T> action) {
        try {
            return CompletableFuture.completedFuture(action.call());
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /// Sends all the given actions to an [ExecutorService] which uses virtual threads and returns the result of
    /// [ExecutorService#invokeAll(Collection)].
    public static <T> List<Future<T>> callAll(Collection<Callable<T>> actions) throws InterruptedException {
        return executor.invokeAll(actions);
    }

    /// Calls [Future#get()] on all the given actions, thus making the current thread wait until all features are done.
    public static <T> void await(Collection<Future<T>> actions) throws InterruptedException, ExecutionException {
        for (Future<?> future : actions) {
            future.get();
        }
    }
}
