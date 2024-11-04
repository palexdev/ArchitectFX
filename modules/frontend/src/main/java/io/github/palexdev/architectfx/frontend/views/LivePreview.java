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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.frontend.components.CountdownIcon;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.model.AppModel;
import io.github.palexdev.architectfx.frontend.model.LivePreviewModel;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.LivePreview.LivePreviewPane;
import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.builders.InsetsBuilder;
import io.github.palexdev.mfxcore.builders.nodes.RegionBuilder;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.ParallelBuilder;
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.ConsumerTransition;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.mfxeffects.animations.motion.Motion;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.utils.ScrollBounds;
import io.inverno.core.annotation.Bean;
import javafx.animation.Animation;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.frontend.theming.ThemeEngine.PAUSED_PSEUDO_CLASS;
import static io.github.palexdev.mfxcore.events.WhenEvent.intercept;
import static io.github.palexdev.mfxcore.observables.When.onChanged;
import static io.github.palexdev.mfxcore.observables.When.onInvalidated;

@Bean
public class LivePreview extends View<LivePreviewPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final Stage mainWindow;
    private final AppModel model;
    private final LivePreviewModel lpModel;
    private final HostServices hostServices;

    //================================================================================
    // Constructors
    //================================================================================
    public LivePreview(IEventBus events, Stage mainWindow, AppModel model, LivePreviewModel lpModel, HostServices hostServices) {
        super(events);
        this.mainWindow = mainWindow;
        this.model = model;
        this.lpModel = lpModel;
        this.hostServices = hostServices;
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
            button("close", e -> {
                lpModel.dispose();
                events.publish(new UIEvent.ViewSwitchEvent(InitView.class));
            }, "Back to Projects Hub");
            toggle("pin", v -> keepOpen = v, keepOpen, "Pin Sidebar");
            addSeparator();

            button("show", e ->
                    Optional.ofNullable(model.getDocument())
                        .map(Pair::getKey)
                        .ifPresent(f -> {
                            try {
                                String parent = f.toPath().getParent().toUri().toString();
                                hostServices.showDocument(parent);
                            } catch (Exception ex) {
                                Logger.warn("Could not show file in file manager because:\n{}", ex);
                            }
                        }),
                "Show in File Manager"
            );
            MFXIconButton playPauseBtn = button("play-pause", e -> lpModel.setPaused(!lpModel.isPaused()), "Play/Pause Scene");
            onInvalidated(lpModel.pausedProperty())
                .then(v -> playPauseBtn.pseudoClassStateChanged(PAUSED_PSEUDO_CLASS, v))
                .executeNow()
                .listen();
            toggle("auto-reload", lpModel::setAutoReload, lpModel.isAutoReload(), "Auto Reload");
            addSeparator();

            MFXIconButton aot = toggle("aot", null, false, "Always on Top");
            aot.selectedProperty().bind(mainWindow.alwaysOnTopProperty());
            aot.setOnAction(e -> mainWindow.setAlwaysOnTop(!mainWindow.isAlwaysOnTop()));
            button("theme-mode", e -> {/*TODO implement*/}, "Light/Dark Mode");

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

        protected MFXIconButton button(String styleClass, EventHandler<ActionEvent> handler, String tooltip) {
            MFXIconButton button = RegionBuilder.region(new MFXIconButton().tonal())
                .addStyleClasses(styleClass)
                .getNode();
            if (handler != null) button.setOnAction(handler);
            if (tooltip != null) UIUtils.installTooltip(button, tooltip, Pos.CENTER_RIGHT);
            getContainerChildren().add(button);
            return button;
        }

        protected MFXIconButton toggle(String styleClass, Consumer<Boolean> selectionHandler, boolean init, String tooltip) {
            MFXIconButton toggle = button(styleClass, null, tooltip).asToggle();
            toggle.setSelected(init);
            if (selectionHandler != null) {
                onInvalidated(toggle.selectedProperty())
                    .then(selectionHandler)
                    .listen();
            }
            return toggle;
        }
    }

    // Content class
    protected class Content extends VFXScrollPane {
        private final StackPane container = new StackPane();
        private Animation animation;

        private final ImageView snapView = new ImageView();
        private boolean disablingPause = false;
        private boolean wasPaused = false;

        {
            setContent(container);
            // Config
            onChanged(model.documentProperty())
                .then((o, n) -> {
                    if (disablingPause) return;
                    Platform.runLater(() -> update(o, n));
                })
                .invalidating(lpModel.pausedProperty())
                .executeNow()
                .listen();
        }

        protected void update(Pair<File, Document> oldDoc, Pair<File, Document> newDoc) {
            lpModel.onDocumentSet(newDoc);
            if (newDoc == null || newDoc.getValue() == null) {
                container.getChildren().clear();
                return;
            }

            // When the document changes, it's not safe to keep the scene "paused" as it could potentially end up
            // displaying an invalid snapshot.
            // For example, if the document loads Nodes with animations, those could be captured at the wrong time.
            // A possible workaround would be to reset the pause state a few seconds/milliseconds
            // after the view is loaded. However, there's no way of telling how much time is needed for it to "stabilize".
            if (oldDoc != null && !Objects.equals(oldDoc, newDoc)) {
                disablingPause = true;
                lpModel.setPaused(false);
                disablingPause = false;
            }

            Parent newRoot = newDoc.getValue().rootNode();
            if (Animations.isPlaying(animation)) animation.stop();
            if (lpModel.isPaused()) {

                ScrollBounds sb = getContentBounds();
                double w = Math.max(sb.contentWidth(), sb.viewportWidth());
                double h = Math.max(sb.contentHeight(), sb.viewportHeight());
                SnapshotParameters parameters = new SnapshotParameters();
                parameters.setFill(Color.TRANSPARENT);

                WritableImage snapshot = UIUtils.snapshot(newRoot, w, h, parameters);
                snapView.setFitWidth(w);
                snapView.setFitHeight(h);
                snapView.setImage(snapshot);
                container.getChildren().setAll(snapView);
                newRoot.setDisable(true);
                wasPaused = true;
            } else if (wasPaused) {
                // If it was paused, do not animate
                newRoot.setDisable(false);
                container.getChildren().setAll(newRoot);
                wasPaused = false;
            } else {
                Parent oldRoot = Optional.ofNullable(oldDoc)
                    .map(Pair::getValue)
                    .map(Document::rootNode)
                    .orElse(null);
                animateUpdate(oldRoot, newRoot);
            }
        }

        protected void animateUpdate(Node oldRoot, Node newRoot) {
            newRoot.setOpacity(0.0);
            container.getChildren().add(newRoot);
            Animation nAnimation = ConsumerTransition.of(
                frac -> newRoot.setOpacity(1.0 * frac),
                M3Motion.SHORT4,
                Motion.EASE_IN
            ).setDelayFluent(M3Motion.SHORT4);
            Animation oAnimation = ConsumerTransition.of(
                frac -> {
                    if (oldRoot == null) return;
                    double target = 1.0 - (1.0 * frac);
                    oldRoot.setOpacity(target);
                },
                M3Motion.SHORT4,
                Motion.EASE_OUT
            );
            Animations.onStopped(oAnimation, () -> container.getChildren().remove(oldRoot), true);
            animation = ParallelBuilder.build()
                .add(oAnimation)
                .add(nAnimation)
                .getAnimation();
            animation.play();
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
