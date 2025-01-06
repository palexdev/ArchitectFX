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

import java.util.Arrays;
import java.util.Map;

import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.architectfx.frontend.components.layout.RootPane;
import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.theming.ThemeMode;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxeffects.animations.Animations.PauseBuilder;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import io.inverno.core.annotation.Bean;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@Bean
@SuppressWarnings("rawtypes")
public class ViewManager {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final Image LIGHT_LOGO = new Image(Resources.loadStream("assets/logo-light.png"));
    public static final Image DARK_LOGO = new Image(Resources.loadStream("assets/logo-dark.png"));

    //================================================================================
    // Properties
    //================================================================================
    private final Stage mainWindow;
    private final RootPane rootPane;
    private final Map<Class<? extends View>, View<?, ?>> views;
    private final ThemeEngine themeEngine;
    private final AppSettings settings;

    //================================================================================
    // Constructors
    //================================================================================
    public ViewManager(
        Stage mainWindow,
        RootPane rootPane,
        Map<Class<? extends View>, View<?, ?>> views,
        IEventBus events,
        ThemeEngine themeEngine,
        AppSettings settings
    ) {
        this.mainWindow = mainWindow;
        this.rootPane = rootPane;
        this.views = views;
        this.themeEngine = themeEngine;
        this.settings = settings;
        events.subscribe(AppEvent.AppReadyEvent.class, e -> onAppReady());
        events.subscribe(UIEvent.ViewSwitchEvent.class, this::onViewSwitchRequest);
        events.subscribe(UIEvent.ThemeSwitchEvent.class, e -> onThemeSwitched());
    }

    //================================================================================
    // Methods
    //================================================================================
    private void onAppReady() {
        onThemeSwitched();
        rootPane.onLayout(this::ensureWindowSizes);
        initMainWindow();
        // Delay centering because of how we handle windows's min sizes
        PauseBuilder.build()
            .setDuration(M3Motion.LONG2)
            .setOnFinished(e -> mainWindow.centerOnScreen())
            .getAnimation()
            .play();
    }

    private void initMainWindow() {
        ((ObjectProperty<ThemeMode>) rootPane.themeModeProperty()).bind(themeEngine.themeModeProperty());

        Size size = settings.getWindowSize();
        Scene scene = new Scene(rootPane, size.getWidth(), size.getHeight(), Color.TRANSPARENT);
        mainWindow.setScene(scene);
        mainWindow.titleProperty().bind(ArchitectFX.windowTitle);
        mainWindow.initStyle(StageStyle.TRANSPARENT);

        onViewSwitchRequest(new UIEvent.ViewSwitchEvent(InitialView.class));
        mainWindow.show();
    }

    private void onViewSwitchRequest(UIEvent.ViewSwitchEvent e) {
        View<?, ?> view = views.get(e.view());
        if (view == null)
            throw new IllegalStateException("Unknown view: " + Arrays.toString(e.data()));

        ArchitectFX.windowTitle.set(ArchitectFX.APP_TITLE + " - " + view.title());

        Region toRegion = view.toRegion();
        if (e.parent() == null) {
            rootPane.setContent(toRegion);
            return;
        }

        Pane root = e.parent();
        if (e.animation() != null) {
            Node old = root.getChildren().isEmpty() ? null : root.getChildren().getFirst();
            if (toRegion != old) e.animation().apply(old, toRegion).play();
            return;
        }
        root.getChildren().setAll(toRegion);
    }

    protected void ensureWindowSizes() {
        Size min = Size.of(
            rootPane.minWidth(-1),
            rootPane.minHeight(-1)
        );
        if (mainWindow.getWidth() < min.getWidth()) mainWindow.setWidth(min.getWidth());
        if (mainWindow.getHeight() < min.getHeight()) mainWindow.setHeight(min.getHeight());
    }

    private void onThemeSwitched() {
        ThemeMode mode = themeEngine.getThemeMode();
        Image icon = mode == ThemeMode.LIGHT ? LIGHT_LOGO : DARK_LOGO;
        mainWindow.getIcons().setAll(icon);
    }
}
