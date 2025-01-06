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

package io.github.palexdev.architectfx.frontend.di;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.palexdev.architectfx.frontend.views.View;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.Wrapper;

@Bean
@Wrapper
@SuppressWarnings("rawtypes")
public class ViewsWrapper implements Supplier<Map<Class<? extends View>, View<?, ?>>> {
    private final Map<Class<? extends View>, View<?, ?>> views;

    public ViewsWrapper(List<View<?, ?>> views) {
        this.views = views.stream()
            .collect(Collectors.toMap(
                View::getClass,
                Function.identity()
            ));
    }

    @Override
    public Map<Class<? extends View>, View<?, ?>> get() {
        return views;
    }
}

