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
import java.util.Map.Entry;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Document;
import io.github.palexdev.architectfx.model.Node;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.config.Config;
import io.github.palexdev.architectfx.utils.ClassScanner;
import io.github.palexdev.architectfx.utils.ReflectionUtils;
import io.github.palexdev.architectfx.utils.Tuple2;
import io.github.palexdev.architectfx.utils.Tuple3;
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
        if (!deps.isEmpty()) {
            DependencyManager.instance()
                .addDeps(deps.toArray(String[]::new))
                .refresh();
        }

        // Handle imports if present
        List<String> imports = parseImports(mappings);
        Logger.debug("Found {} imports", imports.size());
        ClassScanner.setImports(imports);

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

    @SuppressWarnings("unchecked")
    public SequencedMap<String, Property> parseProperties(SequencedMap<String, ?> map) {
        if (map.isEmpty()) return new LinkedHashMap<>();

        Logger.debug("Parsing properties...");
        SequencedMap<String, Property> properties = new LinkedHashMap<>();
        for (Entry<String, ?> e : map.entrySet()) {
            String name = e.getKey();
            Type type = null;
            Object value;

            // Handle metadata
            if (Type.isMetadata(name)) {
                value = switch (name) {
                    case ARGS_TAG, VARARGS_TAG -> parseList(e.getValue()).toArray();
                    case CONFIG_TAG -> parseConfigs(asList(e.getValue(), Map.class));
                    default -> e.getValue();
                };
                type = Type.METADATA;
            } else if ((value = ReflectionUtils.getFieldInfo(e.getValue(), true)) != null) {
                Tuple3<Class<?>, String, Object> tuple = (Tuple3<Class<?>, String, Object>) value;
                if (tuple.a() != null) {
                    type = Type.getType(tuple.c());
                    value = tuple.c();
                }
            } else {
                value = e.getValue();
                type = Property.getPropertyType(name, value);
            }
            if (type == null || value == null) {
                Logger.warn("Skipping property: {}:{}:{}", name, type, value);
                continue;
            }

            Property property = Property.of(name, type, value);
            properties.put(name, property);
            Logger.debug("Parsed property {}", property);
        }
        return properties;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> parseList(Object obj) {
        if (!(obj instanceof List<?>)) {
            Logger.error("Object {} is not a list", Objects.toString(obj));
            return List.of();
        }

        List<Object> list = asGenericList(obj);
        Logger.debug("Value {} is a list, parsing each element...", list);
        List<Object> parsed = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Logger.debug("Parsing element at index {}", i);
            Tuple2<Type, Object> val = parseValue(list.get(i));
            if (val == null) {
                Logger.warn("Parsing failed at index {}, skipping element...", i);
                continue;
            }
            parsed.add(val.b());
        }
        return (List<T>) parsed;
    }

    public List<Config> parseConfigs(List<?> list) {
        if (list == null || list.isEmpty()) return List.of();
        List<Config> configs = new ArrayList<>();
        for (Object o : list) {
            Optional<? extends Config> config = Config.parse(o);
            if (config.isEmpty()) {
                Logger.error("Failed to parse configs...");
                return List.of();
            }
            configs.add(config.get());
        }
        return configs;
    }

    @SuppressWarnings("unchecked")
    public Tuple2<Type, Object> parseValue(Object obj) {
        Object val;

        if ((val = ReflectionUtils.getFieldInfo(obj, true)) != null) {
            Tuple3<Class<?>, String, Object> tuple = (Tuple3<Class<?>, String, Object>) val;
            if (tuple.a() != null) {
                Type type = Type.getType(tuple.c());
                Logger.debug("Value {} is of type: {}", Objects.toString(obj), type);
                return Tuple2.of(type, tuple.c());
            }
        }

        Type type = Type.getType(obj);
        val = switch (type) {
            case COMPLEX -> {
                SequencedMap<String, ?> map = asYamlMap(obj);
                Logger.debug("Value {} is of type: {}", Objects.toString(obj), Type.COMPLEX);
                if (!map.containsKey(TYPE_TAG)) {
                    Logger.error("Could not parse complex value because {} tag is absent, skipping...", TYPE_TAG);
                    yield null;
                }
                yield ReflectionUtils.handleComplexType(map).orElse(null);
            }
            case PRIMITIVE, WRAPPER, STRING -> {
                Logger.debug("Value {} is either of type {} or {} or {}",
                    Objects.toString(obj), Type.PRIMITIVE, Type.WRAPPER, Type.STRING
                );
                yield obj;
            }
            case COLLECTION -> {
                Logger.debug("Value {} is of type: {}", Objects.toString(obj), Type.COLLECTION);
                yield parseList(obj);
            }
            default -> {
                Logger.error("Unsupported value type {}", obj.getClass().getName());
                yield null;
            }
        };
        return (val == null) ? null : Tuple2.of(type, val);
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

    private Node parse(Entry<String, Object> entry) {
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

            Entry<String, Object> asEntry = asMap.firstEntry();
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
