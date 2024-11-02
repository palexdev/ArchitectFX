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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.architectfx.backend.yaml.YamlLoader;
import io.github.palexdev.architectfx.frontend.components.dialogs.ProgressDialog;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.DialogsService;
import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.events.AppEvent.AppCloseEvent;
import io.github.palexdev.architectfx.frontend.events.DialogEvent;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.utils.DateTimeUtils;
import io.github.palexdev.architectfx.frontend.utils.KeyValueProperty;
import io.github.palexdev.architectfx.frontend.utils.ProgressProperty;
import io.github.palexdev.architectfx.frontend.views.LivePreview;
import io.github.palexdev.mfxcomponents.window.popups.PopupWindowState;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.observables.When;
import io.inverno.core.annotation.Bean;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.util.Pair;
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
    private YamlLoader lastLoader;
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
    private final KeyValueProperty<File, Document> document = new KeyValueProperty<>();

    //================================================================================
    // Constructors
    //================================================================================
    public AppModel(AppSettings settings, IEventBus events) {
        this.settings = settings;
        this.events = events;
        this.recents = settings.loadRecents();
        events.subscribe(AppCloseEvent.class, e -> settings.saveRecents(recents));
    }

    //================================================================================
    // Methods
    //================================================================================
    public void run(Tool tool, File file) {
        // Load document async
        loadTask = load(tool, file).thenRun(() -> {
            // Save tool for next session
            lastTool = tool;
            settings.lastTool().set(tool.name());

            // We have to work with lists for simplicity, but we need to make sure that there are no duplicate files!
            Recent recent = new Recent(file.toPath(), DateTimeUtils.epochMilli());
            Set<Recent> tmp = new HashSet<>(recents);
            if (tmp.add(recent)) {
                Platform.runLater(() -> recents.add(recent));
            }

            settings.lastDir().set(file.getParent());
        }).exceptionally(ex -> {
            setProgress(Progress.CANCELED);
            Logger.error(ex.getMessage());
            setDocument(null, null);
            return null;
        });
    }

    protected CompletableFuture<Void> load(Tool tool, File file) {
        // Init loader, show dialog
        lastLoader = tool.loader();
        lastLoader.setOnProgress(this::setProgress);
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
                Document loaded = lastLoader.load(file);
                setDocument(file, loaded);
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    protected void dispose() {
        if (lastLoader != null) lastLoader.close();
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

    public Pair<File, Document> getDocument() {
        return document.get();
    }

    public ReadOnlyObjectProperty<Pair<File, Document>> documentProperty() {
        return document.getReadOnlyProperty();
    }

    protected void setDocument(File file, Document document) {
        this.document.setPair(file, document);
    }
}
