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
import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.backend.utils.reflection.ClassScanner;
import io.github.palexdev.architectfx.backend.utils.reflection.Reflector;
import io.github.palexdev.architectfx.backend.yaml.YamlDeserializer;
import io.github.palexdev.architectfx.backend.yaml.YamlDeserializer.YamlDeserializerConfig;
import io.github.palexdev.architectfx.backend.yaml.YamlLoader;
import io.github.palexdev.architectfx.backend.yaml.YamlParser;

public enum Tool {
    EDIT {
        @Override
        public Document load(File file, Consumer<YamlLoader> loaderConfig) throws IOException {
            throw new UnsupportedOperationException("Not implemented yet.");
        }
    },
    PREVIEW {
        private DependencyManager dm;
        private ClassScanner scanner;
        private Reflector reflector;

        @Override
        public Document load(File file, Consumer<YamlLoader> loaderConfig) throws IOException {
            YamlLoader loader = new YamlLoader();
            loaderConfig.accept(loader);
            return loader.load(file);
        }

        @Override
        public Consumer<YamlLoader> defaultConfig() {
            if (dm == null || scanner == null || reflector == null) {
                dm = new DependencyManager();
                scanner = new ClassScanner(dm);
                reflector = new Reflector(dm, scanner);
            }
            return loader -> loader.withDeserializer(
                () -> new YamlDeserializer(
                    d -> new YamlDeserializerConfig(dm, scanner, reflector, new YamlParser(d, scanner, reflector), true)
                )
            );
        }
    },
    ;

    public abstract Document load(File file, Consumer<YamlLoader> loaderConfig) throws IOException;

    public Document load(File file) throws IOException {
        return load(file, defaultConfig());
    }

    public Consumer<YamlLoader> defaultConfig() {
        return loader -> {};
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
