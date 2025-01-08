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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.utils.OSUtils;
import io.github.palexdev.architectfx.frontend.components.layout.RootPane;
import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.events.SettingsEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.theming.AFXIcons;
import io.github.palexdev.architectfx.frontend.theming.ThemeEngine;
import io.github.palexdev.architectfx.frontend.utils.FileUtils;
import io.github.palexdev.architectfx.frontend.views.ViewManager;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.settings.Settings;
import io.github.palexdev.mfxresources.fonts.IconProvider;
import io.github.palexdev.mfxresources.fonts.IconsProviders;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;
import io.inverno.core.v1.StandardBanner;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import org.tinylog.Logger;

public class ArchitectFX extends Application {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final String APP_TITLE = "ArchitectFX";
    public static final String GIT = "https://github.com/palexdev/ArchitectFX";
    public static final StringProperty windowTitle = new SimpleStringProperty(APP_TITLE);

    // Extra beans
    private static ArchitectFX app;
    private static Stage stage;
    private static Parameters parameters;
    private static HostServices hostServices;
    private static RootPane root;

    // Dependencies
    private static IEventBus events;
    private static ViewManager viewManager;
    private static ThemeEngine themeEngine;
    private static AppSettings settings;

    //================================================================================
    // Startup/Shutdown
    //================================================================================
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            if (e instanceof ConcurrentModificationException)
                // Shut the fuck up
                return;
            Logger.error("!!Exception occurred on the UI thread!!\n {}", e);
        });

        // Init extra beans
        ArchitectFX.app = this;
        ArchitectFX.stage = stage;
        ArchitectFX.parameters = getParameters();
        ArchitectFX.hostServices = getHostServices();
        ArchitectFX.root = new RootPane(stage);

        // Bootstrap
        bootstrap().ifPresentOrElse(
            f -> {
                Logger.info("Bootstrap completed successfully...");
                events.publish(new AppEvent.AppReadyEvent()); // Start app, show main window
            },
            () -> {
                Logger.error("Bootstrap failed, closing app!");
                Platform.exit();
            }
        );
    }

    @Override
    public void stop() {
        if (stage.getWidth() > 0.0) settings.windowWidth().set(stage.getWidth());
        if (stage.getHeight() > 0.0) settings.windowHeight().set(stage.getHeight());
        events.publish(new AppEvent.AppCloseEvent());
    }

    private Optional<Frontend> bootstrap() {
        // First of all, start Inverno modules
        Logger.info(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new StandardBanner().print(new PrintStream(out));
            return out.toString();
        });
        Frontend frontend = io.inverno.core.v1.Application.with(new Frontend.Builder()).run();

        // Ensure supported platform
        if (!OSUtils.isSupportedPlatform()) {
            Logger.error("Unsupported OS detected %s.%nApp will shutdown!".formatted(OSUtils.os()));
            return Optional.empty();
        }

        IconsProviders.registerProvider("afx-", new IconProvider() {
            @Override
            public String getFontPath() {
                return "css/fonts/AFX-Extra.ttf";
            }

            @Override
            public Function<String, Character> getConverter() {
                return AFXIcons::toCode;
            }

            @Override
            public InputStream load() {
                return Resources.loadStream(getFontPath());
            }
        });
        themeEngine.loadTheme();

        // Check if settings reset has been requested via arguments
        // Also add listener for ResetSettingEvents
        if (settings.isResetSettings()) Settings.resetAll();
        events.subscribe(SettingsEvent.ResetSettingsEvent.class, e -> Settings.reset(e.data()));

        return Optional.of(frontend);
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static Path appBaseDir() {
        try {
            return FileUtils.createDirectory(
                Paths.get(System.getProperty("user.home"), ".architectfx")
            );
        } catch (IOException ex) {
            Logger.error("Could not get app base dir:\n{}", ex);
            return null;
        }
    }

    public static Path appCacheDir() {
        try {
            Path baseDir = appBaseDir();
            if (baseDir == null) return null;
            return FileUtils.createDirectory(baseDir.resolve("cache"));
        } catch (IOException ex) {
            Logger.error("Could not get app cache dir because:\n{}", ex);
            return null;
        }
    }

    //================================================================================
    // Sockets
    //================================================================================
    @Bean
    @Wrapper
    public static class App implements Supplier<ArchitectFX> {
        private final ArchitectFX app;

        public App(
            IEventBus events,
            ViewManager viewManger,
            ThemeEngine themeEngine,
            AppSettings settings
        ) {
            app = ArchitectFX.app;
            ArchitectFX.events = events;
            ArchitectFX.viewManager = viewManger;
            ArchitectFX.themeEngine = themeEngine;
            ArchitectFX.settings = settings;
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
    public static class RootPaneWrap implements Supplier<RootPane> {
        @Override
        public RootPane get() {
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
