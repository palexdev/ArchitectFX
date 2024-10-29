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

package io.github.palexdev.architectfx.backend.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Async {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
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

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(command, delay, unit);
    }

    public static <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(callable, delay, unit);
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

    /// @return the [ExecutorService] used by this utility class, which uses virtual threads
    /// @see Executors#newVirtualThreadPerTaskExecutor()
    public static ExecutorService executor() {
        return executor;
    }

    public static ExecutorService schedulingExecutor() {
        return scheduledExecutor;
    }
}
