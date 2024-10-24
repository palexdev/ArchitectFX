package io.github.palexdev.architectfx.frontend.events;

import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcore.events.Event;

public abstract class UIEvent extends Event {

    //================================================================================
    // Constructors
    //================================================================================
    public UIEvent() {}

    public UIEvent(Object data) {
        super(data);
    }

    //================================================================================
    // Impl
    //================================================================================

    @SuppressWarnings("unchecked")
    public static class ViewSwitchEvent extends UIEvent {
        public ViewSwitchEvent(Class<? extends View<?>> view) {
            super(view);
        }

        @Override
        public Class<? extends View<?>> data() {
            return (Class<? extends View<?>>) super.data();
        }
    }
}
