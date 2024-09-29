/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/MaterialFX)
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

package io.github.palexdev.architectfx.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Node;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.utils.ClassScanner;
import io.github.palexdev.architectfx.utils.ReflectionUtils;
import javafx.scene.Parent;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.ARGS_TAG;

// TODO maybe we should not load a Parent but a generic Node
public class YamlLoader {
    //================================================================================
    // Singleton
    //================================================================================
    private static final YamlLoader instance = new YamlLoader();

    public static YamlLoader instance() {
        return instance;
    }

    //================================================================================
    // Constructors
    //================================================================================
    private YamlLoader() {}

    //================================================================================
    // Methods
    //================================================================================

    public Parent load(InputStream stream) throws IOException {
        try {
            SequencedMap<String, Object> mappings = new Yaml().load(stream);
            Document document = YamlDeserializer.instance().parse(mappings);

            // Load dependencies if any
            DependencyManager.instance()
                .addDeps(document.getDependencies().toArray(String[]::new))
                .refresh();

            // Make imports available to the reflection module rather than passing them as args every time
            ClassScanner.setImports(document.getImports());
            return doLoad(document);
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
            Logger.debug("Loading file from url {}", url);
            return load(url.openStream());
        } catch (Exception ex) {
            throw new IOException("Failed to load file", ex);
        }
    }

    private Parent doLoad(Document document) throws IOException {
        Node root = document.getRoot();
        Object[] args = root.getProperty(ARGS_TAG)
            .map(Property::value)
            .map(o -> ((Object[]) o))
            .orElseGet(() -> new Object[0]);
        Parent parent = ReflectionUtils.create(root.getType(), args);
        if (parent == null)
            throw new IOException("Failed to create root node!");

        ReflectionUtils.initialize(parent, root.getProperties().values());
        //handleChildren(document, root, parent);
        return parent;
    }

/*    private void handleChildren(Document document, Node node, Parent parent) throws IOException {
        // TODO handle Group too
        if (parent instanceof Pane pane) {
            for (Node childNode : node.getChildren()) {
                Parent child = init(document, childNode);
                handleChildren(document, childNode, child);
                pane.getChildren().add(child);
            }
        }
    }*/
}
