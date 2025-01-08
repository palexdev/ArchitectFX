/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
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

import java.util.Optional;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.frontend.components.dialogs.DialogType;
import io.github.palexdev.architectfx.frontend.components.dialogs.ProgressDialog;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.DialogConfigurator;
import io.github.palexdev.architectfx.frontend.events.DialogEvent;
import io.github.palexdev.architectfx.frontend.events.ModelEvent;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.utils.FileObserver;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.InitialView;
import io.github.palexdev.architectfx.frontend.views.LivePreviewView;
import io.github.palexdev.mfxcore.base.properties.resettable.ResettableIntegerProperty;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.BeanSocket;
import io.methvin.watcher.DirectoryChangeEvent;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.tinylog.Logger;

@Bean
public class PreviewModel {
    //================================================================================
    // Properties
    //================================================================================
    /* Dependencies */
    private final AppSettings settings;
    private final IEventBus events;
    private final ProjectLoader loader;

    /* State */
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>() {
        @Override
        public void set(Project newValue) {
            if (newValue == null) {
                if (observer != null) {
                    observer.dispose();
                    observer = null;
                }
                setRoot(null);
                super.set(null);
                return;
            }

            Project old = get();
            if (old != newValue) loader.init();
            super.set(newValue);
            loadProject();
        }
    };
    private final ObjectProperty<UILoader.Loaded<Node>> root = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            UILoader.Loaded<Node> root = get();
            if (root != null && root.root() != null) {
                // This is needed for proper observability
                CSSFX.onlyFor(root.root())
                    .addConverter(UIUtils.CSSFX_URI_CONVERTER)
                    .start();
                events.publish(new UIEvent.ViewSwitchEvent(LivePreviewView.class));
                snapshotProject();
            }
        }
    };
    private Task<UILoader.Loaded<Node>> loadTask;

    private FileObserver observer;
    private final ResettableIntegerProperty reloadCountdown = new ResettableIntegerProperty() {
        @Override
        public void reset() {
            boolean isAutoReload = settings.getAutoReload().get();
            int countdown = isAutoReload ? settings.getReloadCountdown().get() : -1;
            set(countdown);
            fireValueChangedEvent(); /* FIXME Resettable properties may be bugged */
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public PreviewModel(AppSettings settings, IEventBus events, ProjectLoader loader) {
        this.settings = settings;
        this.events = events;
        this.loader = loader;
    }

    //================================================================================
    // Methods
    //================================================================================
    public void loadProject() {
        Project project = getProject();
        if (project == null) {
            setRoot(null);
            return;
        }

        if (loadTask != null)
            loadTask.cancel(true);

        loader.progressProperty().reset();
        events.publish(new DialogEvent.ShowDialog<>(DialogType.PROGRESS, () -> new DialogConfigurator.DialogConfig<ProgressDialog>()
            .implicitOwner()
            .setScrimOwner(true)
            .extraConfig(d -> {
                d.progressProperty().bind(loader.progressProperty());
                d.setOnCancel(() -> loadTask.cancel(true));
            })
        ));

        loadTask = new Task<>() {
            @Override
            protected UILoader.Loaded<Node> call() throws Exception {
                return loader.load(project);
            }

            @Override
            protected void succeeded() {
                watchProject();
                setRoot(getValue());
                setReloadCountdown(Integer.MIN_VALUE);
            }

            @Override
            protected void cancelled() {
                setRoot(null);
                setReloadCountdown(-1);
            }

            @Override
            protected void failed() {
                setRoot(null);
                Logger.error("Failed to load project {} because:\n{}", project.getName(), getException());
                setReloadCountdown(-1);
            }
        };
        Async.run(loadTask);
    }

    public Node resolveObj(UIObj obj) {
        return loader.resolveObj(obj);
    }

    protected void watchProject() {
        Project project = getProject();
        observer = FileObserver.observeFile(project.getFile())
            .condition(e -> FileObserver.IS_CHILD.apply(e, project.getFile()))
            .onEvent((e, p) -> {
                if (e == DirectoryChangeEvent.EventType.DELETE) {
                    if (loadTask != null) loadTask.cancel(true);
                    Platform.runLater(() -> {
                        setProject(null);
                        events.publish(new ModelEvent.ProjectDeletedEvent(project));
                        events.publish(new UIEvent.ViewSwitchEvent(InitialView.class));
                    });
                    return;
                }
                reloadCountdown.reset();
            })
            .listen();
    }

    protected void snapshotProject() {
        UIUtils.delayAction(M3Motion.EXTRA_LONG4, () -> {
            Parent parent = Optional.ofNullable(getRoot())
                .map(UILoader.Loaded::root)
                .map(Node::getParent)
                .orElse(null);
            if (parent == null) return;
            Image snap = parent.snapshot(null, null);
            getProject().setPreview(snap);
        });
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Project getProject() {
        return project.get();
    }

    public ObjectProperty<Project> projectProperty() {
        return project;
    }

    public void setProject(Project project) {
        this.project.set(project);
    }

    public UILoader.Loaded<Node> getRoot() {
        return root.get();
    }

    public ObjectProperty<UILoader.Loaded<Node>> rootProperty() {
        return root;
    }

    @BeanSocket(enabled = false)
    public void setRoot(UILoader.Loaded<Node> root) {
        this.root.set(root);
    }

    public int getReloadCountdown() {
        return reloadCountdown.get();
    }

    public ReadOnlyIntegerProperty reloadCountdownProperty() {
        return reloadCountdown.getReadOnlyProperty();
    }

    protected void setReloadCountdown(int reloadCountdown) {
        this.reloadCountdown.set(reloadCountdown);
    }
}
