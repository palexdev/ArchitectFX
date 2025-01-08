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


import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.components.BoundsOverlay;
import io.github.palexdev.architectfx.frontend.components.CountdownIcon;
import io.github.palexdev.architectfx.frontend.components.ObjInspector;
import io.github.palexdev.architectfx.frontend.components.ZoomControls;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.model.PreviewModel;
import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.LivePreviewView.LivePreviewBehavior;
import io.github.palexdev.architectfx.frontend.views.LivePreviewView.LivePreviewPane;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.builders.bindings.ObjectBindingBuilder;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxeffects.animations.Animations;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.ParallelBuilder;
import io.github.palexdev.mfxeffects.animations.ConsumerTransition;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.utils.ScrollBounds;
import io.inverno.core.annotation.Bean;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.application.HostServices;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

@Bean
public class LivePreviewView extends View<LivePreviewPane, LivePreviewBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final AppSettings settings;
    private final ThemeEngine themeEngine;
    private final HostServices hostServices;
    private final PreviewModel previewModel;

    //================================================================================
    // Constructors
    //================================================================================
    public LivePreviewView(IEventBus events, AppSettings settings, ThemeEngine themeEngine, HostServices hostServices, PreviewModel previewModel) {
        super(events);
        this.settings = settings;
        this.themeEngine = themeEngine;
        this.hostServices = hostServices;
        this.previewModel = previewModel;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected LivePreviewPane build() {
        return new LivePreviewPane();
    }

    @Override
    protected Supplier<LivePreviewBehavior> behaviorSupplier() {
        return LivePreviewBehavior::new;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected class LivePreviewPane extends Pane {
        private final BoundsOverlay boundsOverlay;

        private final double H_GAP = 12.0;
        private final Box sidebar;
        private boolean sidebarPinned = true;
        private Animation sidebarAnimation;

        private final VFXScrollPane vsp;

        private final ObjInspector inspector;
        private Animation inspectorAnimation;

        LivePreviewPane() {
            /* Sidebar */
            MFXIconButton pinBtn = new MFXIconButton().asToggle();
            pinBtn.getStyleClass().add("pin");
            pinBtn.setSelected(true);
            pinBtn.setOnAction(e -> sidebarPinned = pinBtn.isSelected());
            UIUtils.installTooltip(pinBtn, "Pin Sidebar");

            MFXIconButton showBtn = new MFXIconButton();
            showBtn.getStyleClass().add("show");
            showBtn.setOnAction(e -> behavior.showInFileManager());
            UIUtils.installTooltip(showBtn, "Show in File Manager");

            CountdownIcon reloadBtn = new CountdownIcon();
            reloadBtn.getStyleClass().add("reload");
            reloadBtn.setOnAction(e -> behavior.reload());
            reloadBtn.setCountdownAction(() -> behavior.reload());
            UIUtils.installTooltip(reloadBtn, "Reload Project");

            MFXIconButton autoReloadBtn = new MFXIconButton().asToggle();
            autoReloadBtn.setSelected(settings.getAutoReload().get());
            autoReloadBtn.getStyleClass().add("auto-reload");
            autoReloadBtn.setOnAction(e -> {
                boolean selected = autoReloadBtn.isSelected();
                if (!selected) reloadBtn.stop();
                behavior.setAutoReload(selected);
            });
            UIUtils.installTooltip(autoReloadBtn, "Enable/Disable Project Auto-Reload");

            When.onInvalidated(previewModel.reloadCountdownProperty())
                .condition(v -> autoReloadBtn.isSelected() && v.intValue() >= 0)
                .then(v -> {
                    if (reloadBtn.isPlaying()) reloadBtn.stop();
                    reloadBtn.setDuration(Duration.seconds(v.intValue()));
                    reloadBtn.play();
                })
                .otherwise((ref, v) -> reloadBtn.stop())
                .listen();

            MFXIconButton inspectBtn = new MFXIconButton().asToggle();
            inspectBtn.getStyleClass().add("inspect");
            UIUtils.installTooltip(inspectBtn, "Open JUI Tree Inspector");

            MFXIconButton themeBtn = new MFXIconButton();
            themeBtn.getStyleClass().add("theme");
            themeBtn.setOnAction(e -> behavior.switchThemeMode());
            UIUtils.installTooltip(themeBtn, "Theme Mode");

            MFXIconButton closeBtn = new MFXIconButton();
            closeBtn.getStyleClass().add("close");
            closeBtn.setOnAction(e -> behavior.close());
            UIUtils.installTooltip(closeBtn, "Close Project");

            /* Zoom Controls */
            ZoomControls zoomControls = new ZoomControls();

            sidebar = new Box(
                Box.Direction.COLUMN,
                pinBtn,
                Box.separator("up"),
                showBtn,
                reloadBtn,
                autoReloadBtn,
                inspectBtn,
                Box.separator(),
                zoomControls,
                Box.separator("down"),
                themeBtn,
                closeBtn
            ).addStyleClass("sidebar");
            When.onInvalidated(sidebar.hoverProperty())
                .then(s -> {
                    if (s) {
                        animateSidebar(true);
                        return;
                    }
                    // Delay sidebar collapse
                    sidebarAnimation = Animations.PauseBuilder.build()
                        .setDuration(M3Motion.EXTRA_LONG4)
                        .setOnFinished(e -> animateSidebar(false))
                        .getAnimation();
                    sidebarAnimation.play();
                })
                .invalidating(sidebar.widthProperty())
                .listen();
            getChildren().add(sidebar);

            /* Content */
            ContentPane contentPane = new ContentPane();
            When.onInvalidated(zoomControls.valueProperty())
                .then(v -> {
                    double scale = v.doubleValue();
                    contentPane.setZoom(scale);
                })
                .listen();

            vsp = new VFXScrollPane(contentPane) {
                @Override
                protected void onContentChanged() {
                    Node content = getContent();
                    if (content == null) {
                        contentBoundsProperty().unbind();
                        setContentBounds(ScrollBounds.ZERO);
                        return;
                    }

                    contentBoundsProperty().bind(ObjectBindingBuilder.<ScrollBounds>build()
                        .setMapper(() -> new ScrollBounds(
                            snapSizeX(content.prefWidth(-1) * zoomControls.getValue()),
                            snapSizeY(content.prefHeight(-1) * zoomControls.getValue()),
                            getViewportSize().getWidth(),
                            getViewportSize().getHeight()
                        ))
                        .addSources(zoomControls.valueProperty())
                        .addSources(content.layoutBoundsProperty())
                        .addSources(viewportSizeProperty())
                        .get()
                    );
                }
            };
            getChildren().add(vsp);

            boundsOverlay = new BoundsOverlay();
            boundsOverlay.scaleProperty().bind(zoomControls.valueProperty());
            getChildren().add(boundsOverlay);
            WhenEvent.intercept(this, ObjInspector.InspectorEvents.SHOW_BOUNDS_OVERLAY)
                .process(e -> {
                    Node node = e.getNode();
                    boundsOverlay.showFor(node);
                })
                .register();

            /* Inspector */
            inspector = new ObjInspector(previewModel::resolveObj);
            When.onInvalidated(inspectBtn.selectedProperty())
                .then(s -> animateInspector(s, true))
                .listen();
            When.onInvalidated(inspector.widthProperty())
                .then(w -> animateInspector(inspectBtn.isSelected(), false))
                .listen();
            getChildren().add(inspector);

            /* Update both content and inspector */
            When.onInvalidated(previewModel.rootProperty())
                .then(r -> {
                    if (r == null) {
                        inspector.setRoot(null);
                        contentPane.setContent(null);
                        return;
                    }
                    contentPane.setContent(r.root());
                    inspector.setRoot(r.document().getRoot());
                })
                .listen();

            getStyleClass().add("live-preview");
        }

        protected void animateSidebar(boolean show) {
            if (Animations.isPlaying(sidebarAnimation))
                sidebarAnimation.stop();

            if (sidebarPinned) {
                sidebar.setTranslateX(0.0);
                sidebar.setOpacity(1.0);
                return;
            }

            double startPos = sidebar.getTranslateX();
            double targetPos = show ? 0.0 : -snapSizeX((sidebar.getWidth() / 1.5));
            double deltaPos = targetPos - startPos;
            double targetOpacity = show ? 1.0 : 0.5;
            Duration duration = M3Motion.MEDIUM4;
            Interpolator curve = M3Motion.STANDARD;

            sidebarAnimation = ParallelBuilder.build()
                .add(KeyFrames.of(duration, sidebar.opacityProperty(), targetOpacity, curve))
                .add(ConsumerTransition.of(
                    f -> {
                        double newVal = startPos + deltaPos * f;
                        sidebar.setTranslateX(newVal);
                        requestLayout(); // This is necessary!
                    },
                    duration,
                    curve)
                ).getAnimation();
            sidebarAnimation.play();
        }

        protected void animateInspector(boolean show, boolean animated) {
            if (Animations.isPlaying(inspectorAnimation))
                inspectorAnimation.stop();

            double startPos = inspector.getTranslateX();
            double targetPos = show ? 0.0 : snapSizeX((inspector.getWidth() + H_GAP));
            double targetOpacity = show ? 1.0 : 0.0;

            if (animated) {
                double deltaPos = targetPos - startPos;
                Duration duration = M3Motion.MEDIUM4;
                Interpolator curve = M3Motion.STANDARD;
                inspectorAnimation = ParallelBuilder.build()
                    .add(KeyFrames.of(duration, inspector.opacityProperty(), targetOpacity, curve))
                    .add(ConsumerTransition.of(
                        f -> {
                            double newVal = startPos + deltaPos * f;
                            inspector.setTranslateX(newVal);
                            requestLayout(); // This is necessary!
                        },
                        duration,
                        curve
                    )).getAnimation();
                inspectorAnimation.play();
            } else {
                inspector.setTranslateX(targetPos);
                inspector.setOpacity(targetOpacity);
                requestLayout();
            }
        }

        @Override
        protected double computeMinWidth(double height) {
            return prefWidth(height);
        }

        @Override
        protected double computePrefWidth(double height) {
            double sW = LayoutUtils.boundWidth(sidebar);
            double contentW = 100.0; // A minimum of 100 for the content
            double iW = 300.0; // A minimum of 300 for the inspector
            double gap = H_GAP * 2;
            return snappedLeftInset() + snapSizeX(sW + contentW + iW + gap) + snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            return prefHeight(width);
        }

        @Override
        protected double computePrefHeight(double width) {
            /* Sidebar */
            double sGap = sidebar.getSpacing();
            double sH = sidebar.getContainerChildren().stream()
                            .filter(n -> n.getClass() != Region.class)
                            .mapToDouble(LayoutUtils::boundHeight)
                            .sum() + ((sidebar.getContainerChildren().size() - 1) * sGap) + 48.0; // Each separator should be at least 24px tall
            return snappedTopInset() +
                   snapSizeY(sidebar.snappedTopInset() + sH + sidebar.snappedBottomInset()) +
                   snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            Rect area = Rect.of(0, 0, getWidth(), getHeight())
                .withInsets(new double[]{
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset()
                });

            /* Overlay */
            boundsOverlay.autosize();

            /* Sidebar */
            area.cutLeft(LayoutUtils.snappedBoundWidth(sidebar))
                .layout(sidebar::resizeRelocate);

            /* Content (dynamic) */
            double maxX = Math.min(getWidth(), inspector.getBoundsInParent().getMinX() - H_GAP);
            Rect contentArea = Rect.of(
                sidebar.getBoundsInParent().getMaxX() + H_GAP, 0,
                maxX, getHeight()
            ).withInsets(new double[]{
                snappedTopInset(),
                snappedRightInset(),
                snappedBottomInset(),
                snappedLeftInset()
            });
            contentArea.layout(vsp::resizeRelocate);

            /* Inspector */
            area.cutRight(getWidth() * 0.3)
                .layout(inspector::resizeRelocate);
        }
    }

    protected class LivePreviewBehavior {

        public void showInFileManager() {
            Project project = previewModel.getProject();
            if (project == null) return;
            hostServices.showDocument(project.getFile().getParent().toString());
        }

        public void reload() {
            previewModel.loadProject();
        }

        public void setAutoReload(boolean autoReload) {
            settings.getAutoReload().set(autoReload);
        }

        public void switchThemeMode() {
            themeEngine.nextMode();
        }

        public void close() {
            events.publish(new UIEvent.ViewSwitchEvent(InitialView.class));
            previewModel.setProject(null);
        }
    }

    protected static class ContentPane extends StackPane {
        private final StackPane wrapper = new StackPane();

        public ContentPane() {
            wrapper.getStyleClass().add("wrapper");
            wrapper.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

            getStyleClass().add("content");
            getChildren().setAll(wrapper);
        }

        public void setContent(Node content) {
            wrapper.getChildren().clear();
            if (content != null) wrapper.getChildren().add(content);
        }

        public void setZoom(double zoom) {
            wrapper.setScaleX(zoom);
            wrapper.setScaleY(zoom);
            requestLayout();
        }
    }
}
