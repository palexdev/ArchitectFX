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

package io.github.palexdev.architectfx.backend.loaders;


import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.palexdev.architectfx.backend.model.UIDocument;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import io.github.palexdev.architectfx.backend.utils.CastUtils;
import io.github.palexdev.architectfx.backend.utils.Progress;

/// Base API and entry point in the system to load and convert a UI document into an actual UI graph that you can visualize.
/// Depending on the framework, a loader must specify how to attach a list of children to a parent by implementing
/// [#attachChildren(Object, List)].
///
/// The result of a load operation is wrapped in a record, see [Loaded].
///
/// @param <T> the type of UI component the loader returns
public interface UILoader<T> {
    Loaded<T> load(UIDocument document) throws IOException;

    Loaded<T> load(InputStream is, URL location) throws IOException;

    default Loaded<T> load(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return load(fis, file.toURI().toURL());
        }
    }

    default Loaded<T> load(URL url) throws IOException {
        return load(url.openStream(), url);
    }

    void attachChildren(T parent, List<T> children) throws IOException;

    default void onProgress(Progress progress) {
        Config config = config();
        if (config == null) return;
        if (config.onProgress == null) return;
        config.onProgress.accept(progress);
    }

    default void onProgress(String description, double progress) {
        onProgress(Progress.of(description, progress));
    }

    Config config();

    //================================================================================
    // Inner Classes
    //================================================================================
    class Config {
        private Function<URI, Resolver> resolverFactory;
        private Consumer<Progress> onProgress;

        public Config() {
            resolverFactory = DefaultResolver::new;
        }

        public Resolver resolver(URI location) {
            return resolverFactory.apply(location);
        }

        public Function<URI, Resolver> getResolverFactory() {
            return resolverFactory;
        }

        public Config setResolverFactory(Function<URI, Resolver> resolverFactory) {
            this.resolverFactory = resolverFactory;
            return this;
        }

        public Consumer<Progress> getOnProgress() {
            return onProgress;
        }

        public Config setOnProgress(Consumer<Progress> onProgress) {
            this.onProgress = onProgress;
            return this;
        }
    }

    /// Expresses the result of a load process by wrapping three pieces of information:
    /// 1) The src document
    /// 2) The root UI component
    /// 3) The controller (may be null)
    record Loaded<T>(UIDocument document, T root, Object controller) {

        // Convenience method to get and cast the controller to the desired class.
        public <C> C controller(Class<C> controllerClass) {
            return CastUtils.as(controller, controllerClass);
        }
    }
}
