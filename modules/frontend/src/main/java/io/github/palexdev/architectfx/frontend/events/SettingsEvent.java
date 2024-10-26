package io.github.palexdev.architectfx.frontend.events;

import io.github.palexdev.mfxcore.events.Event;
import io.github.palexdev.mfxcore.settings.Settings;

public abstract class SettingsEvent extends Event {

    //================================================================================
    // Constructors
    //================================================================================
    public SettingsEvent() {}

    public SettingsEvent(Object data) {
        super(data);
    }

    //================================================================================
    // Impl
    //================================================================================
    public static class ResetSettingsEvent extends SettingsEvent {
        public ResetSettingsEvent(Class<? extends Settings> klass) {
            super(klass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Settings> data() {
            return ((Class<? extends Settings>) super.data());
        }
    }
}
