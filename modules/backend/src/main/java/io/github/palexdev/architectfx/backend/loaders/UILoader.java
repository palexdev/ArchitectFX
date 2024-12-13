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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    /// This class holds data to configure and keep track of the loading process.\
    /// By design, loaders are intended to be reusable, which means that all the data here is retained and used for each
    /// subsequent load. An exception to this "rule" is the `controller factory`, which **should** be reset every time.
    /// The logic is that every document has its own controller, so it's the user's responsibility to set the appropriate
    /// one before the process starts.\
    /// Last but not least, the `controller factory` specified here takes precedence over the controller parsed in the
    /// document specified by [UIDocument] (depends on the loader's implementation though).
    class Config {
        private Supplier<Object> controllerFactory;
        private Function<URI, Resolver> resolverFactory;
        private Consumer<Progress> onProgress;

        public Config() {
            resolverFactory = DefaultResolver::new;
        }

        public Supplier<Object> getControllerFactory() {
            return controllerFactory;
        }

        public Config setControllerFactory(Supplier<Object> controllerFactory) {
            this.controllerFactory = controllerFactory;
            return this;
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
