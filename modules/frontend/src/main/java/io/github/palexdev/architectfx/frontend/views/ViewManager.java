package io.github.palexdev.architectfx.frontend.views;

import java.util.Map;

import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import javafx.scene.layout.Pane;

@Bean
@SuppressWarnings("rawtypes")
public class ViewManager {
    //================================================================================
    // Properties
    //================================================================================
    private final Pane rootPane;
    private final Map<Class<? extends View>, View<?>> views;

    //================================================================================
    // Constructors
    //================================================================================
    public ViewManager(Pane rootPane, Map<Class<? extends View>, View<?>> views, IEventBus events) {
        this.rootPane = rootPane;
        this.views = views;
        events.subscribe(UIEvent.ViewSwitchEvent.class, this::onViewSwitchRequest);
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void onViewSwitchRequest(UIEvent.ViewSwitchEvent event) {
        View<?> view = views.get(event.data());
        if (view == null)
            throw new IllegalStateException("Unknown view: " + event.data());
        rootPane.getChildren().setAll(view.toRegion());
    }
}
