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

package io.github.palexdev.architectfx.frontend.enums;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.loaders.UILoader.Loaded;
import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.utils.reflection.Scanner;
import javafx.scene.Node;

public enum Tool {
    EDIT {
        @Override
        public Loaded<Node> load(File file, Consumer<JUIFXLoader> loaderConfig) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet.");
        }
    },
    PREVIEW {
        private DependencyManager dm;
        private Scanner scanner;
        private Reflector reflector;

        @Override
        public Loaded<Node> load(File file, Consumer<JUIFXLoader> loaderConfig) throws IOException {
            return loader(loaderConfig).load(file);
        }

        @Override
        public Consumer<JUIFXLoader> defaultConfig() {
            if (dm == null || scanner == null || reflector == null) {
                dm = new DependencyManager();
                scanner = new Scanner(dm);
                reflector = new Reflector(scanner);
            }
            return loader -> loader.config().setResolverFactory(
                uri -> {
                    Resolver.Context context = new Resolver.Context(dm, scanner, reflector, uri);
                    return new DefaultResolver(context);
                }
            );
        }

        @Override
        public void dispose() {
            dm = null;
            scanner = null;
            reflector = null;
        }
    },
    ;

    public abstract Loaded<Node> load(File file, Consumer<JUIFXLoader> loaderConfig) throws IOException;

    public Loaded<Node> load(File file) throws IOException {
        return load(file, defaultConfig());
    }

    public JUIFXLoader loader(Consumer<JUIFXLoader> loaderConfig) {
        JUIFXLoader loader = new JUIFXLoader();
        loaderConfig.accept(loader);
        return loader;
    }

    public JUIFXLoader loader() {
        return loader(defaultConfig());
    }

    public Consumer<JUIFXLoader> defaultConfig() {
        return loader -> {};
    }

    public void dispose() {}

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
