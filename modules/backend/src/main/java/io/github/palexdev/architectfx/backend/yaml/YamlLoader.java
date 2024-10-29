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

package io.github.palexdev.architectfx.backend.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.model.Document;
import io.github.palexdev.architectfx.backend.model.Entity;
import io.github.palexdev.architectfx.backend.utils.ChainSupplier;
import io.github.palexdev.architectfx.backend.utils.Progress;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

/// Core class and entry point to load a YAML document compliant with the rules of this system.
///
/// This just offers a bunch of methods to start the loading process and to configure it.
/// The heavy-lifting is done by the [YamlDeserializer].
///
/// **System Flow and Architecture**
///
/// In the following lines, I'm going to briefly describe how the system works internally. Specifically, I want to
/// document the various phases of the loading process.
///
/// - As mentioned above, this is the entry point for the process. By instantiating a `YamlLoader` you can read YAML
/// documents from various sources
/// - The actual work is done by two other core classes: the [YamlDeserializer] and the [YamlParser]
/// 1) The _first phase_ parses document-specific properties, such as imports, dependencies, global configs,...
/// Also, it parses and instantiates every entity/node in the tree structure (see [Entity])
/// 2) This recursive instantiation produces a queue containing all the entities from the top/root.
/// It's basically a flat-view of the tree. The _second phase_ initializes every entity of the queue with the respective
/// properties gathered during the previous step (emphasis on _gathered_, the properties are actually handled/parsed in
/// this step). This phase is also responsible for collecting all the entities that need to be injected in the controller
/// 3) The _third phase_ takes the instantiation queue and rebuilds the tree from it. This can be done thanks to the fact
/// that all entities have their parents already set from the first stage.
/// 4) Finally, the _fourth_phase_ is the one responsible for populating the controller and initializing it
///
/// Supports asynchronous loading and allows to customize the deserializer, see:
/// - [YamlDeserializer#buildTreeConcurrent(Map.Entry)] and [#setParallel(boolean)]
/// - [#withDeserializer(ChainSupplier)]
///
/// Implements [AutoCloseable] for automatic disposal of the [YamlDeserializer] used to load the document ia `try` block.
/// There are cases in which you don't want to dispose the deserializer because that would mean that all its dependencies
/// are going to be disposed as well. If you want to reuse them, for faster subsequent loads of the same document then
/// you can store the dependencies and configure the [YamlDeserializer] factory.
///
/// Allows you to monitor the loading task progress by setting a callback with [#setOnProgress(Consumer)]
// TODO maybe we should not load a Parent but a generic Node
public class YamlLoader implements AutoCloseable {
    //================================================================================
    // Properties
    //================================================================================
    private YamlDeserializer deserializer;
    private ChainSupplier<YamlDeserializer> deserializerFactory;
    private Consumer<Progress> onProgress;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlLoader() {
        this(false);
    }

    public YamlLoader(boolean parallel) {
        deserializerFactory = () -> new YamlDeserializer(parallel);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Entry point for the deserialization and parsing of a YAML document compliant with the rules of this system.
    ///
    /// This is responsible for loading the YAML document in a [SequencedMap] using `SnakeYaml`, and then starting
    /// the four main stages which result in the load of a [Document] instance.
    ///
    /// @throws IOException if an unrecoverable error occurs during the process
    public Document load(InputStream stream) throws IOException {
        try {
            // Load YAML
            onProgress("Loading document...", -1.0);
            SequencedMap<String, Object> map = new Yaml().load(stream);

            // Pre-load document
            deserializer = deserializerFactory.get();
            Document document = deserializer.parseDocument(map);
            onProgress("Nodes instantiated...", 0.75);

            // Initialization stage
            deserializer.initializeTree();
            onProgress("Tree initialized...", 0.9);

            // Finally, build the scene graph and populate the controller if present
            deserializer.linkTree();
            deserializer.handleController(document);
            onProgress("Tree linked and controller initialized...", 0.99);
            onProgress("Done!", 1.0);

            return document;
        } catch (Exception ex) {
            if (onProgress != null)
                onProgress.accept(Progress.CANCELED);
            throw new IOException(ex);
        }
    }

    /// Delegates to [#load(InputStream)] by opening a [FileInputStream] on the given file.
    public Document load(File file) throws IOException {
        try {
            Logger.debug("Loading file {}", file.toString());
            return setDocumentPath(file).load(new FileInputStream(file));
        } catch (Exception ex) {
            throw new IOException("Failed to load file", ex);
        }
    }

    /// Delegates to [#load(InputStream)] by opening a stream with [URL#openStream()].
    public Document load(URL url) throws IOException {
        try {
            Logger.debug("Loading from URL {}", url);
            return load(url.openStream());
        } catch (Exception ex) {
            throw new IOException("Failed to load from URL", ex);
        }
    }

    /// Responsible for signaling the progress state of the load task.
    protected void onProgress(String description, double progress) {
        if (onProgress != null)
            onProgress.accept(Progress.of(description, progress));
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    /// Disposes the last [YamlDeserializer] used to load the document (if not null).
    @Override
    public void close() {
        if (deserializer != null)
            deserializer.dispose();
    }

    //================================================================================
    // Delegate Methods
    //================================================================================

    /// Delegate to [YamlDeserializer#setParallel(boolean)].
    ///
    /// Since a new deserializer is built every time [#load(InputStream)] is called, this applies the configuration
    /// on the factory [#getDeserializerFactory()] thanks to chaining, [ChainSupplier].
    public YamlLoader setParallel(boolean parallel) {
        this.deserializerFactory = deserializerFactory.andThen(d -> d.setParallel(parallel));
        return this;
    }

    /// Delegate to [YamlDeserializer#addToScanCache(Class\[\])].
    ///
    /// Since a new deserializer is built every time [#load(InputStream)] is called, this applies the configuration
    /// on the factory [#getDeserializerFactory()] thanks to chaining, [ChainSupplier].
    public YamlLoader addToScanCache(Class<?>... classes) {
        this.deserializerFactory = deserializerFactory.andThen(d -> d.addToScanCache(classes));
        return this;
    }

    /// Delegate to [YamlDeserializer#setDocumentPath(Path)].
    ///
    /// Since a new deserializer is built every time [#load(InputStream)] is called, this applies the configuration
    /// on the factory [#getDeserializerFactory()] thanks to chaining, [ChainSupplier].
    public YamlLoader setDocumentPath(File file) {
        this.deserializerFactory = deserializerFactory.andThen(d -> d.setDocumentPath(file.toPath()));
        return this;
    }

    /// Delegate to [YamlDeserializer#setControllerFactory(Function)].
    ///
    /// Since a new deserializer is built every time [#load(InputStream)] is called, this applies the configuration
    /// on the factory [#getDeserializerFactory()] thanks to chaining, [ChainSupplier].
    public YamlLoader setControllerFactory(Function<Class<?>, Object> controllerFactory) {
        this.deserializerFactory = deserializerFactory.andThen(d -> d.setControllerFactory(controllerFactory));
        return this;
    }

    /// To be completely honest, the controller factory comes straight from JavaFX's FXMLLoader. Since I don't really
    /// understand why it accepts a class as input, I added this as a shortcut for those who don't need such input.
    /// Why not making it a Supplier and call it a day? Because I'm not excluding that someone out there actually uses
    /// that input in some way.
    ///
    /// Delegate to [#setControllerFactory(Function)].
    public YamlLoader setControllerFactory(Supplier<Object> controllerFactory) {
        this.deserializerFactory = deserializerFactory.andThen(d -> d.setControllerFactory(c -> controllerFactory.get()));
        return this;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================

    /// @return the supplier responsible for building the [YamlDeserializer] used to load the document
    public ChainSupplier<YamlDeserializer> getDeserializerFactory() {
        return deserializerFactory;
    }

    /// Sets the supplier responsible for building the [YamlDeserializer] used to load the document.
    public YamlLoader withDeserializer(ChainSupplier<YamlDeserializer> deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
        return this;
    }

    /// @return the callback that handles progress updates
    public Consumer<Progress> getOnProgress() {
        return onProgress;
    }

    /// Sets the callback that handles progress updates
    public YamlLoader setOnProgress(Consumer<Progress> onProgress) {
        this.onProgress = onProgress;
        return this;
    }
}
