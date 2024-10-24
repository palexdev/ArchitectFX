package io.github.palexdev.architectfx.frontend.di;

import java.util.function.Supplier;

import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.events.bus.SimpleEventBus;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;

@Bean
@Wrapper
public class EventBusWrapper implements Supplier<IEventBus> {
    private final IEventBus events;

    public EventBusWrapper() {
        events = new SimpleEventBus();
    }

    @Override
    public IEventBus get() {
        return events;
    }
}
