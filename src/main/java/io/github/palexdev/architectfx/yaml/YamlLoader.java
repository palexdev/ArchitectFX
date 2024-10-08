package io.github.palexdev.architectfx.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Entity;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
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
/// Also, it parses and instantiates every "top-level" node in the tree structure (see [Entity])
/// 2) This recursive instantiation produces a queue containing all the "top-level" nodes from the top/root.
/// It's basically a flat-view of the tree. The _second phase_ initializes every entity of the queue with the respective
/// properties gathered during the previous step (emphasis on _gathered_, the properties are actually handled/parsed in
/// this step). This phase is also responsible for collecting all the entities that need to be injected in the controller
/// 3) The _third phase_ takes the instantiation queue and rebuilds the tree from it. This can be done thanks to the fact
/// that all entities have their parents already set from the first stage.
/// 4) Finally, the _fourth_phase_ is the one responsible for populating the controller and initializing it
// TODO maybe we should not load a Parent but a generic Node
// TODO make this reusable one way or the other
public class YamlLoader {
    //================================================================================
    // Properties
    //================================================================================
    private YamlDeserializer deserializer;

    //================================================================================
    // Constructors
    //================================================================================
    public YamlLoader() {
        deserializer = new YamlDeserializer();
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
        if (deserializer == null) {
            throw new IllegalStateException("This loader has been closed, use a new one");
        }

        try {
            // Load YAML
            SequencedMap<String, Object> map = new Yaml().load(stream);

            // Pre-load document
            Document document = deserializer.parseDocument(map);

            // Initialization stage
            deserializer.initializeTree();

            // Finally, build the scene graph and populate the controller if present
            deserializer.buildTree();
            deserializer.handleController(document);

            deserializer.dispose();
            deserializer = null;
            return document;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /// Delegates to [#load(InputStream)] by opening a [FileInputStream] on the given file.
    public Document load(File file) throws IOException {
        try {
            Logger.debug("Loading file {}", file.toString());
            return load(new FileInputStream(file));
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

    /// Delegate for [ClassScanner#addToScanCache(Class\[\])].
    public YamlLoader addToScanCache(Class<?>... classes) {
        ClassScanner scanner = deserializer.getScanner();
        scanner.addToScanCache(classes);
        return this;
    }
}
