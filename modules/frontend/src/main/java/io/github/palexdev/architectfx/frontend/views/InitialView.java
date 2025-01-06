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

import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.components.ToggleButton;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.components.layout.Box.Direction;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.InitialView.InitialPane;
import io.github.palexdev.architectfx.frontend.views.InitialView.InitialViewBehavior;
import io.github.palexdev.architectfx.frontend.views.content.PlaceholderView;
import io.github.palexdev.architectfx.frontend.views.content.ProjectsView;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.builders.InsetsBuilder;
import io.github.palexdev.mfxcore.enums.SelectionMode;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.selection.SelectionGroup;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.rectcut.Rect;
import io.inverno.core.annotation.Bean;
import javafx.application.HostServices;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

@Bean
public class InitialView extends View<InitialPane, InitialViewBehavior> {
    //================================================================================
    // Properties
    //================================================================================
    private final ThemeEngine themeEngine;
    private final HostServices hostServices;

    //================================================================================
    // Constructors
    //================================================================================
    public InitialView(IEventBus events, ThemeEngine themeEngine, HostServices hostServices) {
        super(events);
        this.themeEngine = themeEngine;
        this.hostServices = hostServices;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String title() {
        return "Dashboard";
    }

    @Override
    protected InitialPane build() {
        return new InitialPane();
    }

    @Override
    protected Supplier<InitialViewBehavior> behaviorSupplier() {
        return InitialViewBehavior::new;
    }

    @Override
    protected void onAppReady() {
        super.root = build();
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected class InitialPane extends Pane {
        private final Box sidebar;
        private final SelectionGroup selection;
        private final StackPane container;

        InitialPane() {
            // Content
            container = new StackPane();
            container.getStyleClass().add("content");
            getChildren().add(container);

            // Sidebar
            ToggleButton projectsBtn = new ToggleButton("Projects").text();
            projectsBtn.setMinWidth(USE_PREF_SIZE);
            projectsBtn.setMaxWidth(Double.MAX_VALUE);
            projectsBtn.getStyleClass().add("projects");
            projectsBtn.onSelected(() -> behavior.setContent(container, ProjectsView.class));

            ToggleButton dependenciesBtn = new ToggleButton("Dependencies").text();
            dependenciesBtn.setMinWidth(USE_PREF_SIZE);
            dependenciesBtn.setMaxWidth(Double.MAX_VALUE);
            dependenciesBtn.getStyleClass().add("dependencies");
            dependenciesBtn.onSelected(() -> behavior.setContent(container, PlaceholderView.class));

            ToggleButton settingsBtn = new ToggleButton("Settings").text();
            settingsBtn.setMinWidth(USE_PREF_SIZE);
            settingsBtn.setMaxWidth(Double.MAX_VALUE);
            settingsBtn.getStyleClass().add("settings");
            settingsBtn.onSelected(() -> behavior.setContent(container, PlaceholderView.class));

            selection = new SelectionGroup(SelectionMode.SINGLE);
            selection.addAll(projectsBtn, dependenciesBtn, settingsBtn);
            projectsBtn.setSelected(true);

            // Sidebar - Bottom actions
            MFXIconButton gitBtn = new MFXIconButton().tonal();
            gitBtn.setOnAction(e -> behavior.goToGit());
            gitBtn.getStyleClass().add("git");
            UIUtils.installTooltip(gitBtn, "Source Code");

            MFXIconButton themeBtn = new MFXIconButton().tonal();
            themeBtn.setOnAction(e -> behavior.switchThemeMode());
            themeBtn.getStyleClass().add("theme");
            UIUtils.installTooltip(themeBtn, "Theme Mode");

            Box extraActions = new Box(Direction.ROW, gitBtn, themeBtn);
            extraActions.getStyleClass().add("actions");

            // Sidebar
            sidebar = new Box(
                Direction.COLUMN,
                projectsBtn, dependenciesBtn, settingsBtn,
                Box.separator(),
                extraActions
            );
            sidebar.getStyleClass().add("sidebar");
            getChildren().add(sidebar);

            sidebar.setMargin(projectsBtn, InsetsBuilder.left(12.0).get());
            sidebar.setMargin(dependenciesBtn, InsetsBuilder.left(12.0).get());
            sidebar.setMargin(settingsBtn, InsetsBuilder.left(12.0).get());

            getStyleClass().add("initial-view");
            UIUtils.debugTheme(this, "css/views/InitialView.css"); // TODO debug
        }

        @Override
        protected double computeMinWidth(double height) {
            return prefWidth(height);
        }

        @Override
        protected double computePrefWidth(double height) {
            double sidebarW = LayoutUtils.boundWidth(sidebar);
            double containerW = LayoutUtils.boundWidth(container);
            double w = sidebarW + containerW;
            return snappedLeftInset() + snapSizeX(w) + snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            return prefHeight(width);
        }

        @Override
        protected double computePrefHeight(double width) {
            double sidebarH = LayoutUtils.boundHeight(sidebar);
            double containerH = LayoutUtils.boundHeight(container);
            double h = Math.max(sidebarH, containerH);
            return snappedTopInset() + snapSizeY(h) + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            // Layout within bounds
            double w = Math.max(computeMinWidth(-1), getWidth());
            double h = Math.max(computeMinHeight(-1), getHeight());
            Rect area = Rect.of(0, 0, w, h)
                .withInsets(new double[]{
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset()
                });

            // Sidebar
            area.cutLeft(LayoutUtils.snappedBoundWidth(sidebar))
                .layout(sidebar::resizeRelocate);

            // Content
            area.layout(container::resizeRelocate);
        }
    }

    protected class InitialViewBehavior {

        public void setContent(Pane container, Class<? extends View<?, ?>> view) {
            events.publish(new UIEvent.ViewSwitchEvent(
                container,
                view,
                (o, n) -> TimelineBuilder.build()
                    .add(KeyFrames.of(Duration.ONE, e -> {
                        container.setMouseTransparent(true);
                        n.setOpacity(0.0);
                        container.getChildren().add(n);
                    }))
                    .addIf(o != null, () -> KeyFrames.of(M3Motion.MEDIUM1, o.opacityProperty(), 0.0))
                    .add(KeyFrames.of(M3Motion.MEDIUM1, n.opacityProperty(), 1.0))
                    .setOnFinished(e -> {
                        if (o != null) container.getChildren().remove(o);
                        container.setMouseTransparent(false);
                    })
                    .getAnimation()
            ));
        }

        public void goToGit() {
            hostServices.showDocument(ArchitectFX.GIT);
        }

        public void switchThemeMode() {
            themeEngine.nextMode();
        }
    }
}
