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

package io.github.palexdev.architectfx.frontend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.enums.OSType;
import io.github.palexdev.architectfx.backend.utils.OSUtils;
import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.events.SettingsEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.views.ViewManager;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.settings.Settings;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;
import io.inverno.core.v1.StandardBanner;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.pragmatica.lang.Result;
import org.pragmatica.lang.utils.Causes;
import org.tinylog.Logger;
import xss.it.nfx.NfxWindow;

public class ArchitectFX extends Application {
    //================================================================================
    // Properties
    //================================================================================
    public static final String APP_TITLE = "ArchitectFX";
    public static final String GIT = "https://github.com/palexdev/ArchitectFX";
    public static final StringProperty windowTitle = new SimpleStringProperty(APP_TITLE);

    // Module
    private Frontend frontend;

    // Extra beans
    private static ArchitectFX app;
    private static Stage stage;
    private static Parameters parameters;
    private static HostServices hostServices;
    private static StackPane root;

    // Dependencies
    private static ViewManager viewManager;
    private static IEventBus events;
    private static AppSettings settings;
    private static ThemeEngine themeEngine;

    //================================================================================
    // Startup/Shutdown
    //================================================================================
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Init stage
        stage = getMainWindow(stage);

        // Init extra beans
        ArchitectFX.app = this;
        ArchitectFX.stage = stage;
        ArchitectFX.parameters = getParameters();
        ArchitectFX.hostServices = getHostServices();
        ArchitectFX.root = new StackPane() {

            @Override
            protected double computeMinWidth(double height) {
                Node content = getContent();
                return content != null ? content.minWidth(height) + 16.0 : 0.0;
            }

            @Override
            protected double computeMinHeight(double width) {
                Node content = getContent();
                return content != null ? content.minHeight(width) + 40.0 : 0.0;
            }

            Node getContent() {
                return getChildren().isEmpty() ? null : getChildren().getFirst();
            }
        };

        // Bootstrap
        bootstrap().accept(
            fail -> Logger.error(fail.message()),
            success -> {
                frontend = success;
                events.publish(new AppEvent.AppReadyEvent()); // Start app, show main window
                Logger.info("Bootstrap completed successfully!");
            }
        );
    }

    @Override
    public void stop() {
        events.publish(new AppEvent.AppCloseEvent());
        double w = (!Double.isNaN(stage.getWidth()) ? stage.getWidth() : settings.windowWidth().defValue());
        double h = (!Double.isNaN(stage.getHeight()) ? stage.getHeight() : settings.windowHeight().defValue());
        settings.windowWidth().set(w);
        settings.windowHeight().set(h);
    }

    private Result<Frontend> bootstrap() {
        // First of all, start Inverno modules
        Logger.info(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new StandardBanner().print(new PrintStream(out));
            return out.toString();
        });
        Frontend frontend = io.inverno.core.v1.Application.with(new Frontend.Builder()).run();

        // Ensure supported platform
        if (!OSUtils.isSupportedPlatform()) {
            return Result.err(Causes.cause("Unsupported OS detected %s.%nApp will shutdown!".formatted(OSUtils.os())));
        }

        // TODO init theme before anything else
        themeEngine.loadTheme();

        // Check if settings reset has been requested via arguments
        // Also add listener for ResetSettingEvents
        if (settings.isResetSettings()) Settings.resetAll();
        events.subscribe(SettingsEvent.ResetSettingsEvent.class, e -> Settings.reset(e.data()));

        return Result.success(frontend);
    }

    //================================================================================
    // Misc
    //================================================================================
    private Stage getMainWindow(Stage stage) {
        // Special stage only for Windows
        if (OSUtils.os() == OSType.Windows) {
            NfxWindow window = new NfxWindow();
            window.titleBarColorProperty().bind(
                window.sceneProperty()
                    .flatMap(Scene::rootProperty)
                    .flatMap(r -> ((Region) r).backgroundProperty())
                    .map(b -> {
                        List<BackgroundFill> fills = Optional.ofNullable(b)
                            .map(Background::getFills)
                            .orElse(List.of());
                        Color bg = fills.isEmpty() ? Color.WHITE : (Color) fills.getFirst().getFill();
                        Logger.debug("Bg will be: {}", bg);
                        return bg;
                    })
            );
            stage = window;
        }
        stage.titleProperty().bind(windowTitle);
        return stage;
    }

    //================================================================================
    // Sockets
    //================================================================================
    @Bean
    @Wrapper
    public static class App implements Supplier<ArchitectFX> {
        private final ArchitectFX app;

        public App(
            ViewManager viewManger,
            IEventBus events,
            AppSettings settings,
            ThemeEngine themeEngine
        ) {
            app = ArchitectFX.app;
            ArchitectFX.viewManager = viewManger;
            ArchitectFX.events = events;
            ArchitectFX.settings = settings;
            ArchitectFX.themeEngine = themeEngine;
        }

        @Override
        public ArchitectFX get() {
            return app;
        }
    }

    @Bean
    @Wrapper
    public static class MainWindow implements Supplier<Stage> {
        @Override
        public Stage get() {
            return ArchitectFX.stage;
        }
    }

    @Bean
    @Wrapper
    public static class RootPane implements Supplier<Pane> {
        @Override
        public Pane get() {
            return ArchitectFX.root;
        }
    }

    @Bean
    @Wrapper
    public static class ParametersWrap implements Supplier<Parameters> {
        @Override
        public Parameters get() {
            return ArchitectFX.parameters;
        }
    }

    @Bean
    @Wrapper
    public static class HostServicesWrap implements Supplier<HostServices> {
        @Override
        public HostServices get() {
            return ArchitectFX.hostServices;
        }
    }

}
