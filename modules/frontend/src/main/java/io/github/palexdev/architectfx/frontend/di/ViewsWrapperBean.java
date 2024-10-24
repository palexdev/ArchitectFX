package io.github.palexdev.architectfx.frontend.di;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.views.base.View;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;

@Bean
@Wrapper
@SuppressWarnings("rawtypes")
public class ViewsWrapperBean implements Supplier<Map<Class<? extends View>, View<?>>> {
    private final Map<Class<? extends View>, View<?>> views;

    public ViewsWrapperBean() {
        this.views = new HashMap<>();
    }

    @Override
    public Map<Class<? extends View>, View<?>> get() {
        return views;
    }
}
