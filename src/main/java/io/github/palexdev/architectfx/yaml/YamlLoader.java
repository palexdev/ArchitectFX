package io.github.palexdev.architectfx.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.model.Document;
import javafx.scene.Parent;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

// TODO maybe we should not load a Parent but a generic Node
public class YamlLoader {

    //================================================================================
    // Methods
    //================================================================================
    public Parent load(InputStream stream) throws IOException {
        try {
            // Load YAML
            SequencedMap<String, Object> map = new Yaml().load(stream);

            // Pre-load document
            YamlDeserializer deserializer = new YamlDeserializer();
            Document document = deserializer.parseDocument(map);

            // Initialization stage
            deserializer.initializeTree();

            // Finally, build the scene graph
            Parent root = deserializer.buildSceneGraph(document);
            deserializer.dispose();
            return root;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public Parent load(File file) throws IOException {
        try {
            Logger.debug("Loading file {}", file.toString());
            return load(new FileInputStream(file));
        } catch (Exception ex) {
            throw new IOException("Failed to load file", ex);
        }
    }

    public Parent load(URL url) throws IOException {
        try {
            Logger.debug("Loading from URL {}", url);
            return load(url.openStream());
        } catch (Exception ex) {
            throw new IOException("Failed to load from URL", ex);
        }
    }
}
