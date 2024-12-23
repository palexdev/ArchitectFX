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
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.architectfx.frontend.components.dialogs.ProgressDialog;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.DialogsService;
import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.events.AppEvent.AppCloseEvent;
import io.github.palexdev.architectfx.frontend.events.DialogEvent;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.utils.ProgressProperty;
import io.github.palexdev.architectfx.frontend.views.LivePreview;
import io.github.palexdev.mfxcomponents.window.popups.PopupWindowState;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.observables.When;
import io.inverno.core.annotation.Bean;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.tinylog.Logger;

@Bean
public class AppModel {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableList<Recent> recents;
    private final AppSettings settings;
    private final IEventBus events;

    private Tool lastTool;
    private JUIFXLoader lastLoader;
    private CompletableFuture<?> loadTask;
    private final ProgressProperty progress = new ProgressProperty() {
        @Override
        protected void invalidated() {
            if (get() == Progress.CANCELED && loadTask != null) {
                loadTask.cancel(true);
                Logger.debug("Load task has been canceled");
                loadTask = null;
            }
        }
    };
    private final ReadOnlyObjectWrapper<UILoader.Loaded<Node>> loaderResult = new ReadOnlyObjectWrapper<>() {
        @Override
        protected void invalidated() {
            UILoader.Loaded<Node> result = get();
            if (result != null) {
                // FIXME I don't remember why, probably to supports live css changes, needs testing (try-catch error!)
                CSSFX.onlyFor(result.root())
                    .addConverter(uri -> {
                        try {
                            return Paths.get(URI.create(uri));
                        } catch (Exception ignored) {}
                        return null;
                    })
                    .start();
            }
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public AppModel(AppSettings settings, IEventBus events) {
        this.settings = settings;
        this.events = events;
        this.recents = FXCollections.observableArrayList(settings.loadRecents());
        events.subscribe(AppCloseEvent.class, e -> settings.saveRecents(recents));
    }

    //================================================================================
    // Methods
    //================================================================================
    public void run(Tool tool, URI uri) {
        // Load document async
        loadTask = load(tool, uri).thenRun(() -> {
            // Save tool for next session
            lastTool = tool;
            settings.lastTool().set(tool.name());

            // We have to work with lists for simplicity, but we need to make sure that there are no duplicate files!
            Path toPath = Path.of(uri);
            Recent recent = new Recent(toPath);
            Set<Recent> tmp = new HashSet<>(recents);
            if (tmp.add(recent)) {
                Platform.runLater(() -> {
                    recents.add(recent);
                    FXCollections.sort(recents);
                });
            }

            settings.lastDir().set(toPath.getParent().toString());
        }).exceptionally(ex -> {
            setProgress(Progress.CANCELED);
            Logger.error(ex.getMessage());
            setLoaderResult(null);
            return null;
        });
    }

    protected CompletableFuture<Void> load(Tool tool, URI uri) {
        // Init loader, show dialog
        lastLoader = tool.loader();
        lastLoader.config().setOnProgress(this::setProgress);
        progress.reset(); // Needed otherwise subsequent calls will not make the dialog appear
        events.publish(new DialogEvent.ShowProgress(() -> new DialogsService.DialogConfig<ProgressDialog>()
            .implicitOwner()
            .setScrimOwner(true)
            .extraConfig(d -> {
                d.progressProperty().bind(progress);
                // Switch view once closed for better transition
                When.onInvalidated(d.stateProperty())
                    .condition(PopupWindowState::isClosing)
                    .then(s -> {
                        if (getProgress() != Progress.CANCELED)
                            Platform.runLater(
                                () -> events.publish(new UIEvent.ViewSwitchEvent(LivePreview.class))
                            );
                    })
                    .oneShot()
                    .listen();
            })
        ));

        return Async.run(() -> {
            try {
                UILoader.Loaded<Node> loaded = lastLoader.load(Path.of(uri).toFile());
                setLoaderResult(loaded);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    protected void dispose() {
        if (lastTool != null) lastTool.dispose();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public ObservableList<Recent> recents() {
        return recents;
    }

    public Tool getLastTool() {
        return lastTool;
    }

    public Progress getProgress() {
        return progress.get();
    }

    public ReadOnlyObjectProperty<Progress> progressProperty() {
        return progress.getReadOnlyProperty();
    }

    protected void setProgress(Progress progress) {
        this.progress.set(progress);
    }

    public UILoader.Loaded<Node> getLoaderResult() {
        return loaderResult.get();
    }

    public ReadOnlyObjectProperty<UILoader.Loaded<Node>> loaderResultProperty() {
        return loaderResult.getReadOnlyProperty();
    }

    protected void setLoaderResult(UILoader.Loaded<Node> loaderResult) {
        this.loaderResult.set(loaderResult);
    }
}
