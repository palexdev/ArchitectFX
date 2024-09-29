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

import java.io.IOException;
import java.util.*;

import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Node;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.Step;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.utils.CastUtils.*;
import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.*;

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

        // Handle dependencies if present
        List<String> deps = parseDependencies(mappings);
        Logger.debug("Found {} dependencies:\n{}", deps.size(), deps);

        // Handle imports if present
        List<String> imports = parseImports(mappings);
        Logger.debug("Found {} imports", imports.size());

        // Handle controller if present
        String controller = parseController(mappings);
        Logger.debug("Controller {}found", controller != null ? "" : "not ");

        if (mappings.size() > 1)
            Logger.warn("Document is probably malformed. {} root nodes detected, trying to parse anyway...", mappings.size());

        Node root = parse(mappings.firstEntry());
        Document document = new Document(root, controller);
        document.getDependencies().addAll(deps);
        document.getImports().addAll(imports);
        return document;
    }

    public SequencedMap<String, Property> parseProperties(SequencedMap<String, ?> map) {
        if (map.isEmpty()) return new LinkedHashMap<>();

        Logger.debug("Parsing properties...");
        SequencedMap<String, Property> properties = new LinkedHashMap<>();
        for (Map.Entry<String, ?> e : map.entrySet()) {
            String name = e.getKey();
            Type type;
            Object value;

            // Handle metadata
            if (Type.isMetadata(name)) {
                value = switch (name) {
                    case ARGS_TAG -> e.getValue(); // TODO implement parsing
                    case STEPS_TAG -> parseSteps(asList(e.getValue(), Map.class));
                    default -> e.getValue();
                };
                type = Type.METADATA;
            } else {
                value = e.getValue();
                type = Property.getPropertyType(name, value);
            }
            if (value == null) continue;

            Property property = Property.of(name, type, value);
            properties.put(name, property);
            Logger.debug("Parsed property {}", property);
        }
        return properties;
    }

    // TODO parse arguments! (since we already do it for Collections we can commonize the code)

    public List<Step> parseSteps(List<?> yamlSteps) {
        if (yamlSteps == null || yamlSteps.isEmpty()) return List.of();
        List<Step> steps = new ArrayList<>();
        for (Object yamlStep : yamlSteps) {
            Optional<Step> step = parseStep(yamlStep);
            if (step.isEmpty()) {
                Logger.error("Failed to parse steps...");
                return List.of();
            }
            steps.add(step.get());
        }
        return steps;
    }

    private Optional<Step> parseStep(Object yamlStep) {
        if (yamlStep instanceof SequencedMap<?, ?>) {
            SequencedMap<String, Object> map = asYamlMap(yamlStep);
            if (!map.containsKey(NAME_TAG)) {
                Logger.error("Invalid step because no name was found");
                return Optional.empty();
            }

            String name = (String) map.get(NAME_TAG);
            Object[] args = Optional.ofNullable(map.get(ARGS_TAG))
                .map(o -> ((List<?>) o).toArray())
                .orElseGet(() -> new Object[0]);
            boolean transform = Optional.ofNullable(map.get(TRANSFORM_TAG))
                .map(o -> Boolean.parseBoolean(o.toString()))
                .orElse(false);
            return Optional.of(new Step(name, args).setTransform(transform));
        }
        Logger.error("Invalid step because object {} is not a valid YAML map", yamlStep);
        return Optional.empty();
    }

    private List<String> parseDependencies(SequencedMap<String, ?> map) {
        Object depsObj = null;
        if (map.containsKey(DEPS_TAG)) {
            depsObj = map.remove(DEPS_TAG);
        } else if (map.containsKey(DEPENDENCIES_TAG)) {
            depsObj = map.remove(DEPENDENCIES_TAG);
        }
        return Optional.ofNullable(depsObj)
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    private List<String> parseImports(SequencedMap<String, ?> map) {
        return Optional.ofNullable(map.remove(IMPORTS_TAG))
            .filter(List.class::isInstance)
            .map(l -> asList(l, String.class))
            .orElseGet(List::of);
    }

    private String parseController(SequencedMap<String, ?> map) {
        return Optional.ofNullable(map.remove(CONTROLLER_TAG))
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

        if (!children.isEmpty()) Logger.debug("Parsing children...");
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
        SequencedMap<String, Property> parsedProperties = parseProperties(properties);
        node.getProperties().putAll(parsedProperties);
        return node;
    }
}
