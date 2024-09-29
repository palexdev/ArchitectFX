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

package io.github.palexdev.architectfx.utils;

import java.util.*;

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.enums.Type;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.Step;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.*;

public class ReflectionUtils {
    //================================================================================
    // Constructors
    //================================================================================
    private ReflectionUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static <T> T create(Class<?> klass, Object... args) {
        try {
            Logger.trace("Attempting to create class {} with args: {}", klass.getName(), Arrays.toString(args));
            return Reflect.onClass(klass.getName(), DependencyManager.instance().getClassLoader())
                .create(args)
                .get();
        } catch (Exception ex) {
            Logger.error("Failed to create class {} because: {}", klass.getName(), ex.getMessage());
            return null;
        }
    }

    public static <T> T create(String className, Object... args) {
        try {
            Class<?> klass = ClassScanner.findClass(className);
            return create(klass, args);
        } catch (ClassNotFoundException | IllegalStateException ex) {
            Logger.error(ex, "Failed to create class {}", className);
            return null;
        }
    }

    public static <T> Optional<T> createOpt(String className, Object... args) {
        return Optional.ofNullable(create(className, args));
    }

    public static <T> T invokeFactory(String factoryName, Object... args) {
        String[] split = factoryName.split("\\.");
        String className = split[0];
        String method = split[1];
        try {
            Logger.trace(
                "Attempting to call factory {} with args: {}\n Class: {}\n Static Method: {}",
                factoryName, Arrays.toString(args), className, method
            );
            Class<?> klass = ClassScanner.findClass(className);
            return Reflect.onClass(klass.getName(), DependencyManager.instance().getClassLoader())
                .call(method, args)
                .get();
        } catch (Exception ex) {
            Logger.error("Failed to invoke factory {} because: {}", factoryName, ex.getMessage());
            return null;
        }
    }

    public static <T> Optional<T> invokeFactoryOpt(String factoryName, Object... args) {
        return Optional.ofNullable(invokeFactory(factoryName, args));
    }

    public static void initialize(Object obj, Collection<Property> properties) {
        Logger.debug("Initializing object {}", obj.getClass().getName());
        for (Property p : properties) {
            String name = p.name();
            Logger.trace(p);

            // Handle metadata (for now only .steps is relevant)
            if (name.equals(STEPS_TAG)) {
                List<Step> steps = CastUtils.asList(p.value(), Step.class);
                Optional<Object> opt = Optional.of(obj);
                for (Step step : steps) {
                    if (opt.isEmpty()) break;
                    opt = step.run(opt.get());
                }
                continue;
            }
            if (p.type() == Type.METADATA) continue;

            // Handle static
            if (name.contains(".")) {
                Logger.error("Static field or method detected for name {}", name); // TODO change this once supported
                // TODO implement
                System.err.println("Unsupported operation: static handling");
                continue;
            }

            switch (p.type()) {
                case ENUM -> handleEnum(obj, p);
                case PRIMITIVE, WRAPPER, STRING -> handlePrimitive(obj, p);
                case COMPLEX -> handleComplexType(CastUtils.asYamlMap(p.value())).ifPresentOrElse(
                    o -> {
                        String setter = resolveSetter(name);
                        Reflect.on(obj).call(setter, o);
                    },
                    () -> Logger.error("Could not set complex object {}, skipping...")
                );
                case COLLECTION -> handleCollection(obj, p);
                case UNKNOWN -> Logger.error("Unsupported type {} for field {}, skipping...", p.type(), name);
            }
        }
    }

    private static void handleEnum(Object obj, Property property) {
        String name = property.name();
        Object value = property.value();
        if (value instanceof Enum<?> eValue) {
            // Do it via setter
            try {
                String setter = resolveSetter(name);
                Logger.debug("Attempting to set enum value {} via setter {}", value, setter);
                Reflect.on(obj).call(setter, eValue);
            } catch (ReflectException ex) {
                Logger.error(ex, "Failed to set enum value.");
            }
        }
    }

    private static void handlePrimitive(Object obj, Property property) {
        String name = property.name();
        Object value = property.value();
        try {
            // Do it via setter method
            String setter = resolveSetter(name);
            Logger.debug("Attempting to set 'primitive' type {} to value {} via setter {}", value.getClass().getSimpleName(), value, setter);
            Reflect.on(obj).call(setter, value);
        } catch (Exception ex) {
            Logger.error(ex, "Failed to set 'primitive' value.");
        }
    }

    public static Optional<Object> handleComplexType(SequencedMap<String, ?> map) {
        // We need to clone the map because of the following remove operations
        // We ideally do not want to alter the original map
        //
        // The removals happen so that we avoid the parseProperties step if not necessary.
        // An example of this would be metadata such as .type and .args which are already used here
        //
        // For metadata like .steps we rely on initialize(...)
        SequencedMap<String, ?> tmp = new LinkedHashMap<>(map);
        String type = (String) tmp.remove(TYPE_TAG);
        if (type == null) return Optional.empty();

        // Extract args if present
        Object[] args = Optional.ofNullable(tmp.remove(ARGS_TAG))
            .map(YamlDeserializer.instance()::parseList)
            .map(List::toArray)
            .orElseGet(() -> new Object[0]);

        Optional<Object> opt;
        if (map.containsKey(FACTORY_TAG)) { // Handle factories/builders
            opt = handleFactory(type, tmp, args);
        } else { // Standard object instantiation
            Logger.debug("Creating complex type {} with args {}", type, Arrays.toString(args));
            opt = createOpt(type, args);
        }

        if (opt.isEmpty()) return Optional.empty();

        // Extract properties and initialize the object
        if (!tmp.isEmpty()) {
            Logger.debug("Extracting properties for complex type...");
            SequencedMap<String, Property> properties = YamlDeserializer.instance().parseProperties(tmp);
            Logger.trace("Properties: {}", properties);
            initialize(opt.get(), properties.values());
        }
        return opt;
    }

    private static Optional<Object> handleFactory(String type, SequencedMap<String, ?> map, Object[] args) {
        String factory = (String) map.remove(FACTORY_TAG);
        if (factory == null || !factory.contains(".")) {
            Logger.error(
                "Could not create complex type {} through factory because the name {} is invalid",
                type, factory
            );
            return Optional.empty();
        }
        return invokeFactoryOpt(factory, args);
    }

    private static void handleCollection(Object obj, Property property) {
        String name = property.name();
        List<Object> values = CastUtils.asGenericList(property.value());
        Logger.debug("Value {} is a collection...", values);

        // For now, collections handling requires a getter in the target object
        // to retrieve the collection.
        Collection<? super Object> collection;
        try {
            String getter = resolveGetter(name);
            Logger.debug("Retrieving collection via getter {}", getter);
            collection = Reflect.on(obj).call(getter).get();
            assert collection != null;
            Logger.trace("Retrieved collection of type {} with size {}", collection.getClass(), collection.size());
        } catch (AssertionError | ReflectException err) {
            Logger.error(err, "Failed to retrieve collection, skipping...");
            return;
        }

        List<Object> parsed = YamlDeserializer.instance().parseList(values);
        Logger.debug("Adding parsed elements {} to collection...", parsed);
        collection.addAll(parsed);
    }

    public static String resolveGetter(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static String resolveSetter(String fieldName) {
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}