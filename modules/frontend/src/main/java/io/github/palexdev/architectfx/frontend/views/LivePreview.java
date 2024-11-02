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

package io.github.palexdev.architectfx.frontend.views;

import java.io.File;
import java.util.function.Consumer;

import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.frontend.components.CountdownIcon;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.model.AppModel;
import io.github.palexdev.architectfx.frontend.model.LivePreviewModel;
import io.github.palexdev.architectfx.frontend.views.LivePreview.LivePreviewPane;
import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.builders.InsetsBuilder;
import io.github.palexdev.mfxcore.builders.nodes.RegionBuilder;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.ConsumerTransition;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.utils.ScrollBounds;
import io.inverno.core.annotation.Bean;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;
import static io.github.palexdev.mfxcore.observables.When.onInvalidated;

@Bean
public class LivePreview extends View<LivePreviewPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final Stage mainWindow;
    private final AppModel model;
    private final LivePreviewModel lpModel;

    //================================================================================
    // Constructors
    //================================================================================
    public LivePreview(IEventBus events, Stage mainWindow, AppModel model, LivePreviewModel lpModel) {
        super(events);
        this.mainWindow = mainWindow;
        this.model = model;
        this.lpModel = lpModel;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected LivePreviewPane build() {
        return new LivePreviewPane();
    }

    @Override
    public String title() {
        return "Live Preview";
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected class LivePreviewPane extends HBox {
        private final double padding = 4.0;
        private final FileModifiedIcon fmIcon;

        {
            // Sidebar
            Sidebar sidebar = new Sidebar();
            HBox.setMargin(sidebar, InsetsBuilder.build().withVertical(padding).get());

            // Content
            Content content = new Content();
            HBox.setMargin(content, InsetsBuilder.uniform(padding).get());
            HBox.setHgrow(content, Priority.ALWAYS);

            // File modified icon
            fmIcon = new FileModifiedIcon();
            fmIcon.setManaged(false);

            getChildren().addAll(sidebar, content, fmIcon);
            getStyleClass().add("preview");
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            // Manual layout for file modified icon
            double mul = 3.0;
            layoutInArea(
                fmIcon,
                getLayoutX(), getLayoutY(),
                getWidth(), getHeight(), 0,
                InsetsBuilder.of(padding * mul, padding * mul, 0.0, 0.0),
                HPos.RIGHT, VPos.TOP
            );
        }
    }

    // Sidebar class
    protected class Sidebar extends Box {
        private final double padding = 4.0;
        private final double sidebarOffset = 20.0;
        private boolean keepOpen = false;

        private Animation animation;

        {
            // Buttons
            button("close", e -> events.publish(new UIEvent.ViewSwitchEvent(InitView.class)));
            toggle("pin", v -> keepOpen = v, keepOpen);

            button("play-pause", e -> lpModel.setPaused(!lpModel.isPaused()));
            toggle("auto-reload", lpModel::setAutoReload, lpModel.isAutoReload());

            MFXIconButton aot = toggle("aot", null, false);
            aot.selectedProperty().bind(mainWindow.alwaysOnTopProperty());
            aot.setOnAction(e -> mainWindow.setAlwaysOnTop(!mainWindow.isAlwaysOnTop()));

            button("theme-mode", e -> {/*TODO implement*/});

            // Config
            onInvalidated(hoverProperty())
                .then(s -> {
                    if (s) {
                        animate(true);
                        return;
                    }
                    // Delay sidebar collapse
                    animation = PauseBuilder.build()
                        .setDuration(M3Motion.EXTRA_LONG4)
                        .setOnFinished(e -> animate(false))
                        .getAnimation();
                    animation.play();
                })
                .invalidating(widthProperty())
                .listen();
            getStyleClass().add("sidebar");
        }

        protected void animate(boolean show) {
            if (Animations.isPlaying(animation))
                animation.stop();

            if (keepOpen) {
                HBox.setMargin(this, InsetsBuilder.of(padding, 0.0, padding, 0.0));
                return;
            }

            Insets margin = HBox.getMargin(this);
            double start = margin.getLeft();
            double target = show ? 0.0 : -this.getWidth() + sidebarOffset;
            double delta = target - start;

            animation = ConsumerTransition.of(
                f -> {
                    double newVal = start + delta * f;
                    HBox.setMargin(this, InsetsBuilder.of(padding, 0.0, padding, newVal));
                },
                M3Motion.MEDIUM2,
                M3Motion.STANDARD
            );
            animation.play();
        }

        protected void button(String styleClass, EventHandler<ActionEvent> handler) {
            MFXIconButton button = RegionBuilder.region(new MFXIconButton().tonal())
                .addStyleClasses(styleClass)
                .getNode();
            button.setOnAction(handler);
            getChildren().add(button);
        }

        protected MFXIconButton toggle(String styleClass, Consumer<Boolean> selectionHandler, boolean init) {
            MFXIconButton button = RegionBuilder.region(new MFXIconButton().tonal().asToggle())
                .addStyleClasses(styleClass)
                .getNode();
            button.setSelected(init);
            if (selectionHandler != null) {
                onInvalidated(button.selectedProperty())
                    .then(selectionHandler)
                    .listen();
            }
            return button;
        }
    }

    // Content class
    protected class Content extends VFXScrollPane {
        private final ImageView snapView = new ImageView();

        {
            // Config
            onInvalidated(model.documentProperty())
                .then(fd -> Platform.runLater(() -> update(fd)))
                .invalidating(lpModel.pausedProperty())
                .executeNow()
                .listen();
        }

        protected void update(Pair<File, Document> fd) {
            if (fd == null || fd.getValue() == null) {
                setContent(null);
                return;
            }

            Parent root = fd.getValue().rootNode();
            if (lpModel.isPaused()) {
                ScrollBounds sb = getContentBounds();
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                params.setViewport(new Rectangle2D(
                    0, 0,
                    Math.max(sb.contentWidth(), sb.viewportWidth()),
                    Math.max(sb.contentHeight(), sb.viewportHeight())
                ));
                WritableImage snap = root.snapshot(params, null);
                snapView.setImage(snap);
                setContent(snapView);
                root.setDisable(true);
            } else {
                root.setDisable(false);
                snapView.setImage(null);
                lpModel.onDocumentSet(fd);
                setContent(root);
            }
        }
    }

    // File modified icon class
    protected class FileModifiedIcon extends CountdownIcon {
        private Animation animation;

        {
            setPickOnBounds(false);
            countdownProperty().bind(lpModel.reloadDelayProperty());
            statusProperty().bindBidirectional(lpModel.countdownStatusProperty());
            setOpacity(0.0);
            setVisible(false);

            onInvalidated(lpModel.fileModifiedProperty())
                .then(this::animate)
                .listen();
            intercept(this, MouseEvent.MOUSE_CLICKED)
                .condition(e -> e.getButton() == MouseButton.PRIMARY)
                .process(e -> lpModel.reloadDelayed(0))
                .register();
        }

        protected void animate(boolean modified) {
            if (Animations.isPlaying(animation))
                animation.stop();

            if (modified) {
                animation = TimelineBuilder.build()
                    .add(KeyFrames.of(0, e -> {
                        setOpacity(0.0);
                        setVisible(true);
                        setDisable(false);
                    }))
                    .add(KeyFrames.of(M3Motion.MEDIUM4, opacityProperty(), 0.6, M3Motion.STANDARD))
                    .getAnimation();
            } else {
                animation = TimelineBuilder.build()
                    .add(KeyFrames.of(0, e -> setDisable(true)))
                    .add(KeyFrames.of(M3Motion.MEDIUM4, opacityProperty(), 0.0, M3Motion.STANDARD))
                    .setOnFinished(e -> setVisible(false))
                    .getAnimation();
            }
            animation.play();
        }
    }
}
