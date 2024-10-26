package io.github.palexdev.architectfx.frontend.views.base;

import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public abstract class View<P extends Pane> {
    //================================================================================
    // Properties
    //================================================================================
    protected P root;
    protected final IEventBus events;

    //================================================================================
    // Constructors
    //================================================================================
    protected View(IEventBus events) {
        this.events = events;
        events.subscribe(AppEvent.AppReadyEvent.class, e -> onAppReady());
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract P build();

    //================================================================================
    // Methods
    //================================================================================
    public Region toRegion() {
        if (root == null) {
            root = build();
        }
        return root;
    }

    public String title() {
        return "";
    }

    protected void onAppReady() {}
}
