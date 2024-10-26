package io.github.palexdev.architectfx.frontend.views;

import java.util.Map;

import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@Bean
@SuppressWarnings("rawtypes")
public class ViewManager {
    //================================================================================
    // Properties
    //================================================================================
    private final Stage mainWindow;
    private final Pane rootPane;
    private final Map<Class<? extends View>, View<?>> views;
    private final AppSettings settings;

    //================================================================================
    // Constructors
    //================================================================================
    public ViewManager(
        Stage mainWindow,
        Pane rootPane,
        Map<Class<? extends View>, View<?>> views, IEventBus events,
        AppSettings settings
    ) {
        this.mainWindow = mainWindow;
        this.rootPane = rootPane;
        this.views = views;
        this.settings = settings;
        events.subscribe(AppEvent.AppReadyEvent.class, e -> onAppReady());
        events.subscribe(UIEvent.ViewSwitchEvent.class, this::onViewSwitchRequest);
    }

    //================================================================================
    // Methods
    //================================================================================
    private void initMainWindow() {
        Scene scene = new Scene(rootPane);
        mainWindow.setScene(scene);
        mainWindow.initStyle(StageStyle.UNIFIED);
        onViewSwitchRequest(new UIEvent.ViewSwitchEvent(InitView.class));
        mainWindow.show();
    }

    private void onAppReady() {
        initMainWindow();
        mainWindow.sizeToScene();
        mainWindow.centerOnScreen();
        mainWindow.setResizable(false);
    }

    private void onViewSwitchRequest(UIEvent.ViewSwitchEvent event) {
        View<?> view = views.get(event.data());
        if (view == null)
            throw new IllegalStateException("Unknown view: " + event.data());
        ArchitectFX.windowTitle.set(ArchitectFX.APP_TITLE + " - " + view.title());
        rootPane.getChildren().setAll(view.toRegion());
    }
}
