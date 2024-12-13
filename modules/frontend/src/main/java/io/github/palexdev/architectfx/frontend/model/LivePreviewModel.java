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

package io.github.palexdev.architectfx.frontend.model;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.architectfx.frontend.components.CountdownIcon.CountdownStatus;
import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.BeanSocket;
import io.methvin.watcher.DirectoryChangeEvent.EventType;
import io.methvin.watcher.DirectoryWatcher;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.util.Duration;
import org.tinylog.Logger;

@Bean
public class LivePreviewModel {
    //================================================================================
    // Properties
    //================================================================================
    private final AppModel appModel;
    private final AppSettings settings;

    private URI oldLocation;
    private DirectoryWatcher watcher;
    private CompletableFuture<?> watcherTask;
    private boolean autoReload;
    private final ReadOnlyBooleanWrapper fileModified = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyObjectWrapper<Duration> reloadDelay = new ReadOnlyObjectWrapper<>();
    private final ObjectProperty<CountdownStatus> countdownStatus = new SimpleObjectProperty<>(CountdownStatus.STOPPED) {
        @Override
        protected void invalidated() {
            if (get() == CountdownStatus.FINISHED)
                reload();
        }
    };
    private final BooleanProperty paused = new SimpleBooleanProperty(false);

    //================================================================================
    // Constructors
    //================================================================================
    public LivePreviewModel(AppModel appModel, AppSettings settings, IEventBus events) {
        this.appModel = appModel;
        this.settings = settings;
        this.autoReload = settings.autoReload().get();
        setReloadDelay(Duration.seconds(settings.autoReloadDelay().get()));

        events.subscribe(AppEvent.AppCloseEvent.class, e -> {
            settings.autoReload().set(isAutoReload());
            settings.autoReloadDelay().set((int) getReloadDelay().toSeconds());
        });
    }

    //================================================================================
    // Methods
    //================================================================================

    public void onDocumentSet(UILoader.Loaded<Node> result) {
        if (result == null) {
            closeWatcher();
            return;
        }
        watchDocument(result);
        oldLocation = result.document().getLocation();
    }

    protected void watchDocument(UILoader.Loaded<Node> result) {
        if (result == null ||
            Objects.equals(result.document().getLocation(), oldLocation)
        ) return;

        if (watcher != null && !watcher.isClosed())
            throw new IllegalStateException("Previous watcher is still running...");

        try {
            URI location = result.document().getLocation();
            if (Objects.equals(location, oldLocation)) return;

            Path toPath = Path.of(location);
            watcher = DirectoryWatcher.builder()
                .path(toPath)
                .fileHashing(true)
                .listener(e -> {
                    if (e.path().equals(toPath) && e.eventType() == EventType.MODIFY) {
                        setFileModified(true);
                        if (isAutoReload()) {
                            int delay = settings.autoReloadDelay().get();
                            reloadDelayed(delay);
                        }
                    }
                })
                .build();
            watcherTask = Async.run(watcher::watch);
        } catch (IOException ex) {
            Logger.error("Failed to build file watcher:\n{}", ex);
        }
    }

    protected void closeWatcher() {
        if (watcher != null) {
            if (watcherTask != null) {
                watcherTask.cancel(true);
                watcherTask = null;
            }
            try {
                watcher.close();
            } catch (IOException ex) {
                Logger.error("Failed to close file watcher:\n{}", ex);
            } finally {
                watcher = null;
            }
        }
    }

    public void reloadDelayed(int delay) {
        // Stop current load and countdown
        appModel.setProgress(Progress.CANCELED);
        setCountdownStatus(CountdownStatus.STOPPED);

        // If is manual reload, don't start countdown
        if (delay == 0) {
            reload();
            return;
        }

        // Set delay and restart countdown
        setReloadDelay(Duration.seconds(delay));
        setCountdownStatus(CountdownStatus.STARTED);
    }

    protected void reload() {
        Tool tool = appModel.getLastTool();
        try {
            setFileModified(false);
            appModel.load(tool, oldLocation).get();
        } catch (Exception ex) {
            Logger.error("Failed to reload document because:\n{}", ex);
            setFileModified(true);
        }
    }

    public void dispose() {
        oldLocation = null;
        closeWatcher();
        appModel.dispose();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public boolean isAutoReload() {
        return autoReload;
    }

    @BeanSocket(enabled = false)
    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }

    public boolean isFileModified() {
        return fileModified.get();
    }

    public ReadOnlyBooleanProperty fileModifiedProperty() {
        return fileModified.getReadOnlyProperty();
    }

    protected void setFileModified(boolean fileModified) {
        this.fileModified.set(fileModified);
    }

    public Duration getReloadDelay() {
        return reloadDelay.get();
    }

    public ReadOnlyObjectProperty<Duration> reloadDelayProperty() {
        return reloadDelay.getReadOnlyProperty();
    }

    protected void setReloadDelay(Duration reloadDelay) {
        this.reloadDelay.set(reloadDelay);
    }

    public CountdownStatus getCountdownStatus() {
        return countdownStatus.get();
    }

    public ObjectProperty<CountdownStatus> countdownStatusProperty() {
        return countdownStatus;
    }

    protected void setCountdownStatus(CountdownStatus status) {
        this.countdownStatus.set(status);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused.set(paused);
    }
}
