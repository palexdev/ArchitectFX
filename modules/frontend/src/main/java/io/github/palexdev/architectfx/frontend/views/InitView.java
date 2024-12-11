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

import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.components.vfx.RecentsGrid;
import io.github.palexdev.architectfx.frontend.components.vfx.cells.RecentCell;
import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.model.AppModel;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.architectfx.frontend.views.InitView.InitPane;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcore.builders.InsetsBuilder;
import io.github.palexdev.mfxcore.controls.Text;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxeffects.animations.Animations.AbstractBuilder;
import io.github.palexdev.mfxeffects.animations.Animations.KeyFrames;
import io.github.palexdev.mfxeffects.animations.Animations.ParallelBuilder;
import io.github.palexdev.mfxeffects.animations.Animations.TimelineBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.github.palexdev.rectcut.Rect;
import io.inverno.core.annotation.Bean;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.application.HostServices;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

@Bean
public class InitView extends View<InitPane> {
    //================================================================================
    // Properties
    //================================================================================
    private final Stage mainWindow;
    private final ThemeEngine themeEngine;
    private final AppModel model;
    private final AppSettings settings;
    private final HostServices hostServices;

    //================================================================================
    // Constructors
    //================================================================================
    public InitView(
        IEventBus events,
        Stage mainWindow, ThemeEngine themeEngine,
        AppModel model, AppSettings settings,
        HostServices hostServices
    ) {
        super(events);
        this.mainWindow = mainWindow;
        this.themeEngine = themeEngine;
        this.model = model;
        this.settings = settings;
        this.hostServices = hostServices;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected InitPane build() {
        return new InitPane();
    }

    @Override
    protected void onAppReady() {
        super.root = build();
    }

    @Override
    protected void onSwitching() {
        super.root.animateText();
    }

    @Override
    public String title() {
        return "Project Hub";
    }

    //================================================================================
    // View Class
    //================================================================================
    protected class InitPane extends StackPane {
        private static final String HEADER = "Welcome to Your Project Hub!";
        private static final String SUB_HEADER = "Explore your projects\nContinue where you left off or import new ones";

        private final double H_SPACING = 12.0;
        private final double V_SPACING = 8.0;

        // Top Left
        private final ImageView logoView;
        private final Text title;
        private final Text header;
        private final Text subHeader;
        private final Box topBox;

        // Bottom Left
        private final MFXIconButton gitBtn;
        private final MFXIconButton aotBtn;
        private final MFXIconButton themeBtn;
        private final Box bottomBox;

        // Right
        private final RecentsGrid grid;

        protected InitPane() {
            // Top Left
            logoView = new ImageView();

            title = new Text(ArchitectFX.APP_TITLE);
            title.getStyleClass().add("title");

            header = new Text(HEADER);
            header.getStyleClass().add("header");

            subHeader = new Text(SUB_HEADER);
            subHeader.getStyleClass().add("sub-header");

            topBox = new Box(
                Box.Direction.COLUMN,
                logoView, title,
                header, subHeader
            );
            topBox.getStyleClass().add("top");
            VBox.setMargin(title, InsetsBuilder.top(-24.0).get()); // Offset because of the image
            VBox.setMargin(header, InsetsBuilder.top(24.0).get()); // Extra space between logo and header

            // Bottom Left
            gitBtn = UIUtils.iconButton(
                "git",
                (b, e) -> hostServices.showDocument(ArchitectFX.GIT),
                "Project's Home",
                Pos.BOTTOM_CENTER
            );

            themeBtn = UIUtils.iconButton(
                "theme-mode",
                (b, e) -> themeEngine.nextMode(),
                "Light/Dark Mode",
                Pos.BOTTOM_CENTER
            );

            aotBtn = UIUtils.iconToggle(
                "aot",
                (b, s) -> mainWindow.setAlwaysOnTop(!s),
                false,
                "Always on Top",
                Pos.BOTTOM_CENTER
            );
            aotBtn.selectedProperty().bind(mainWindow.alwaysOnTopProperty());


            bottomBox = new Box(
                Box.Direction.ROW,
                gitBtn,
                themeBtn,
                aotBtn
            );
            bottomBox.getStyleClass().add("bottom");

            grid = new RecentsGrid(model.recents(), settings.lastDir().get());
            WhenEvent.intercept(grid, RecentCell.RecentCellEvent.ANY)
                .process(e -> {
                    switch (e.getEventType()) {
                        case EventType<?> t when t == RecentCell.RecentCellEvent.LIVE_PREVIEW_EVENT ->
                            model.run(Tool.PREVIEW, e.getRecent().file().toFile());
                        case EventType<?> t when t == RecentCell.RecentCellEvent.FILE_EXPLORER_EVENT ->
                            hostServices.showDocument(e.getRecent().file().getParent().toUri().toString());
                        case EventType<?> t when t == RecentCell.RecentCellEvent.REMOVE_EVENT ->
                            model.recents().remove(e.getRecent());
                        case null, default -> {}
                    }
                })
                .register();

            getStyleClass().add("init-view");
            getChildren().addAll(topBox, bottomBox, grid);
        }

        @Override
        protected double computeMinWidth(double height) {
            return snappedLeftInset() +
                   Math.max(
                       LayoutUtils.snappedBoundWidth(topBox),
                       LayoutUtils.snappedBoundWidth(bottomBox)
                   ) + H_SPACING +
                   LayoutUtils.snappedBoundWidth(grid) +
                   snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            double lH = LayoutUtils.snappedBoundHeight(topBox) +
                        V_SPACING +
                        LayoutUtils.snappedBoundHeight(bottomBox);
            double rH = LayoutUtils.snappedBoundHeight(grid);
            return snappedTopInset() + Math.max(lH, rH) + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            double[] insets = new double[]{
                snappedTopInset(),
                snappedRightInset(),
                snappedBottomInset(),
                snappedLeftInset(),
            };
            Rect area = Rect.of(0, 0, w, h)
                .withHSpacing(H_SPACING)
                .withInsets(insets);

            double gridW = LayoutUtils.snappedBoundWidth(grid);
            area.cutRight(gridW).layout(grid::resizeRelocate);

            area.withVSpacing(V_SPACING)
                .cutBottom(LayoutUtils.snappedBoundHeight(bottomBox))
                .layout(bottomBox::resizeRelocate);
            area.layout(topBox::resizeRelocate);
        }

        protected void animateText() {
            Duration delay = (!mainWindow.isShowing()) ? M3Motion.EXTRA_LONG4 : Duration.ZERO;
            Interpolator curve = M3Motion.STANDARD_ACCELERATE;
            new ParallelBuilder() {
                double distance = 0.0;

                @Override
                public AbstractBuilder show(Duration duration, Node... nodes) {
                    for (Node node : nodes) {
                        KeyFrame kf0 = KeyFrames.of(Duration.ZERO, node.opacityProperty(), 0.0);
                        KeyFrame fk1 = KeyFrames.of(duration, node.opacityProperty(), 1.0, curve);
                        add(TimelineBuilder.build()
                            .add(kf0, fk1)
                            .setDelay(distance)
                            .getAnimation());
                    }
                    distance += M3Motion.MEDIUM4.toMillis();
                    return this;
                }
            }
                .show(M3Motion.MEDIUM4, logoView, title)
                .show(M3Motion.LONG4, header)
                .show(M3Motion.LONG4, subHeader)
                .setDelay(delay)
                .getAnimation()
                .play();
        }
    }
}
