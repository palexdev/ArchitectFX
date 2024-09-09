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

import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Node;
import io.github.palexdev.architectfx.model.Property;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;

import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.*;

public class YamlDeserializer {
    //================================================================================
    // Singleton
    //================================================================================
    private static final YamlDeserializer instance = new YamlDeserializer();

    public static YamlDeserializer instance() {
        return instance;
    }

    //================================================================================
    // Constructors
    //================================================================================
    private YamlDeserializer() {}

    //================================================================================
    // Methods
    //================================================================================
    public Document parse(SequencedMap<String, Object> mappings) throws IOException {
        if (mappings.isEmpty()) throw new IOException("Failed to parse document, cause: empty");
        if (mappings.size() > 2) {
            Logger.warn("Document is probably malformed. {} root nodes detected, trying to parse anyway...", mappings.size());
        }

        // Handle dependencies if present
        List<String> deps = parseDependencies(mappings);
        Logger.debug("Found {} dependencies:\n{}", deps.size(), deps);

        // Handle imports if present
        List<String> imports = parseImports(mappings);
        Logger.debug("Found {} imports", imports.size());

        // Handle controller if present
        String controller = parseController(mappings);
        Logger.debug("Controller {}found", controller != null ? "" : "not ");

        Node root = parse(mappings.firstEntry());
        Document document = new Document(root, controller);
        document.getDependencies().addAll(deps);
        document.getImports().addAll(imports);
        return document;
    }

    private List<String> parseDependencies(SequencedMap<String, ?> map) {
        Object depsObj = null;
        if (map.containsKey("deps")) {
            depsObj = map.remove("deps");
        } else if (map.containsKey("dependecies")) {
            depsObj = map.remove("dependencies");
        }
        return Optional.ofNullable(depsObj)
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    private List<String> parseImports(SequencedMap<String, ?> map) {
        return Optional.ofNullable(map.remove("imports"))
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    private String parseController(SequencedMap<String, ?> map) {
        return Optional.ofNullable(map.remove("controller"))
            .filter(String.class::isInstance)
            .map(o -> as(o, String.class))
            .orElse(null);
    }

    private Node parse(Map.Entry<String, Object> entry) {
        String type = entry.getKey();
        SequencedMap<String, Object> properties = asYamlMap(entry.getValue());
        Node node = new Node(type);
        Logger.debug("Parsing node of type {}", type);
        Logger.trace("Properties:\n{}", properties);

        // Handle children if present
        List<?> children = Optional.ofNullable(properties.remove("children"))
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .orElseGet(List::of);
        if (!children.isEmpty())
            Logger.debug("Parsing children...");

        for (Object child : children) {
            SequencedMap<String, Object> asMap = asYamlMap(child);
            // Each child is a map of length 1
            // In case it's not issue a warning
            if (asMap.isEmpty()) {
                Logger.warn("Children map is empty");
                continue;
            }
            if (asMap.size() > 1)
                Logger.warn(
                    "Expected size 1 for children collection, found {}. Trying to parse anyway.",
                    asMap.size()
                );

            Map.Entry<String, Object> asEntry = asMap.firstEntry();
            Node childNode = parse(asEntry);
            node.getChildren().add(childNode);
            Logger.debug("Added child {} to node {}", childNode, node);
        }

        // Handle properties
        if (!properties.isEmpty()) Logger.debug("Parsing properties...");
        for (Map.Entry<String, Object> pEntry : properties.entrySet()) {
            String pName = pEntry.getKey();
            String pType = Property.getPropertyType(pName, pEntry.getValue());
            Object value = pEntry.getValue();
            Property property = new Property(pName, pType, value);
            node.getProperties().add(property);
            Logger.debug("Added property: {}", property);
        }
        return node;
    }
}
