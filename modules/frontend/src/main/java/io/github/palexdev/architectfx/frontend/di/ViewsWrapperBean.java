package io.github.palexdev.architectfx.frontend.di;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.palexdev.architectfx.frontend.views.base.View;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;

@Bean
@Wrapper
@SuppressWarnings("rawtypes")
public class ViewsWrapperBean implements Supplier<Map<Class<? extends View>, View<?>>> {
    private final Map<Class<? extends View>, View<?>> views;

    public ViewsWrapperBean(List<View<?>> views) {
        this.views = views.stream()
            .collect(Collectors.toMap(
                View::getClass,
                Function.identity()
            ));
    }

    @Override
    public Map<Class<? extends View>, View<?>> get() {
        return views;
    }
}
