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

package io.github.palexdev.architectfx.frontend.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.palexdev.architectfx.backend.utils.Async;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryWatcher;
import io.methvin.watcher.hashing.FileHasher;
import io.methvin.watcher.visitor.FileTreeVisitor;
import org.tinylog.Logger;

public class FileObserver {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final BiFunction<DirectoryChangeEvent, Path, Boolean> IS_CHILD = (e, p) ->
        e.path().equals(p) && e.eventType() == DirectoryChangeEvent.EventType.MODIFY;

    public static final FileTreeVisitor FILES_VISITOR = (file, onDirectory, onFile) -> {
        if (!Files.isDirectory(file)) return;
        try (Stream<Path> stream = Files.list(file)) {
            Path[] files = stream.filter(f -> !Files.isDirectory(f))
                .toArray(Path[]::new);
            for (Path f : files) {
                onFile.call(f);
            }
        } catch (IOException ex) {
            Logger.error("File observer error:\n{}", ex);
        }
    };

    //================================================================================
    // Properties
    //================================================================================
    private final Path path;
    private Function<DirectoryChangeEvent, Boolean> condition = p -> true;
    private Consumer<Path> onChanged = e -> {};

    private DirectoryWatcher watcher;
    private Future<?> task;

    //================================================================================
    // Constructors
    //================================================================================
    public FileObserver(Path path) {
        this.path = path;
    }

    public static FileObserver observeFile(Path path) {
        return new FileObserver(path.getParent());
    }

    //================================================================================
    // Methods
    //================================================================================
    public FileObserver condition(Function<DirectoryChangeEvent, Boolean> condition) {
        this.condition = condition;
        return this;
    }

    public FileObserver onChanged(Consumer<Path> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public FileObserver executeNow() {
        onChanged.accept(path);
        return this;
    }

    public FileObserver listen() {
        try {
            watcher = DirectoryWatcher.builder()
                .path(path)
                .fileHashing(true)
                .fileHasher(FileHasher.LAST_MODIFIED_TIME)
                .fileTreeVisitor(FILES_VISITOR)
                .listener(e -> {
                    if (condition.apply(e))
                        onChanged.accept(path);
                })
                .build();
            task = watcher.watchAsync(Async.executor());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public void dispose() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        if (watcher != null) {
            try {
                watcher.close();
                watcher = null;
            } catch (Exception ignored) {}
        }
    }
}
