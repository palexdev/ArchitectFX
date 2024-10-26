package io.github.palexdev.architectfx.frontend;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.enums.OSType;
import io.github.palexdev.architectfx.backend.utils.OSUtils;
import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.views.ViewManager;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;
import io.inverno.core.v1.StandardBanner;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
        ArchitectFX.root = new StackPane();

        // Bootstrap
        bootstrap().accept(
            fail -> Logger.error(fail.message()),
            success -> {
                frontend = success;
                events.publish(new AppEvent.AppReadyEvent());
                Logger.info("Bootstrap completed successfully!");
            }
        );

        stage.setScene(new Scene(new Group(), 800, 600));
        stage.show();
    }

    private Result<Frontend> bootstrap() {
        // First of all, start Inverno modules
        Logger.info(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new StandardBanner().print(new PrintStream(out));
            return out.toString();
        });
        io.inverno.core.v1.Application.with(new Frontend.Builder()).run();

        // Ensure supported platform
        if (!OSUtils.isSupportedPlatform()) {
            return Result.err(Causes.cause("Unsupported OS detected %s.%nApp will shutdown!".formatted(OSUtils.os())));
        }

        // TODO init theme before anything else

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
            IEventBus events
        ) {
            app = ArchitectFX.app;
            ArchitectFX.viewManager = viewManger;
            ArchitectFX.events = events;
        }

        @Override
        public ArchitectFX get() {
            return app;
        }
    }

    @Bean
    @Wrapper
    public static class RootPane implements Supplier<Pane> {
        private final Pane rootPane;

        public RootPane() {
            this.rootPane = ArchitectFX.root;
        }

        @Override
        public Pane get() {
            return rootPane;
        }
    }
}
