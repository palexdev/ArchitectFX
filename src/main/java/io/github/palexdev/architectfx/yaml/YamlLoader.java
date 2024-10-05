package io.github.palexdev.architectfx.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.utils.reflection.ClassScanner;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

// TODO maybe we should not load a Parent but a generic Node
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
            deserializer.buildSceneGraph();
            deserializer.handleController(document);

            deserializer.dispose();
            deserializer = null;
            return document;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public Document load(File file) throws IOException {
        try {
            Logger.debug("Loading file {}", file.toString());
            return load(new FileInputStream(file));
        } catch (Exception ex) {
            throw new IOException("Failed to load file", ex);
        }
    }

    public Document load(URL url) throws IOException {
        try {
            Logger.debug("Loading from URL {}", url);
            return load(url.openStream());
        } catch (Exception ex) {
            throw new IOException("Failed to load from URL", ex);
        }
    }

    public YamlLoader addToScanCache(Class<?>... classes) {
        ClassScanner scanner = deserializer.getScanner();
        scanner.addToScanCache(classes);
        return this;
    }
}
