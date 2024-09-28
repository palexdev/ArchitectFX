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

import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.model.Step;
import io.github.palexdev.architectfx.yaml.YamlDeserializer;
import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.*;

public class ReflectionUtils {
	//================================================================================
	// Static Properties
	//================================================================================
	private static final Set<Class<?>> primitives = Set.of(
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        String.class // Special case
    );

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
		Logger.debug("Initializing object {}", obj);
		for (Property p : properties) {
			String name = p.getName();
			String type = p.getType();
			Object value = p.getValue();
			Logger.trace(p);

			// Handle static
			if (name.contains(".")) {
				Logger.trace("Static field or method detected for name {}", name);
				// TODO implement
				System.err.println("Unsupported operation: static handling");
				continue;
			}

			// Handle enum types
			if (handleEnum(obj, name, value)) continue;

			// Handle "primitive" types
			if (handlePrimitive(obj, name, value)) continue;

			// Handle complex types (maps)
			if (value instanceof SequencedMap<?, ?> map) {
				handleComplexType(type, CastUtils.asYamlMap(map)).ifPresentOrElse(
					o -> {
						String setter = resolveSetter(name);
						Reflect.on(obj).call(setter, o);
					},
					() -> Logger.error("Could not set complex object {}, skipping...")
				);
				continue;
			}

			// Handle collections
			if (value instanceof List<?> list) {
				Logger.debug("Value {} is a collection...", list);
				handleCollection(obj, name, list);
				continue;
			}

    		// Fallback: Unsupported Type
    		Logger.error("Unsupported type {} for field {}, skipping...", type, name);
		}
	}

	private static boolean handleEnum(Object obj, String fieldName, Object value) {
		if (value instanceof String sValue) {
			Optional<Enum<?>> eValue = isEnum(sValue);
			if (eValue.isEmpty()) return false;

			// Do it via setter
			try {
				String setter = resolveSetter(fieldName);
				Logger.debug("Attempting to set enum value {} via setter {}", value, setter);
				Reflect.on(obj).call(setter, eValue.get());
				return true;
			} catch (ReflectException ex) {
                Logger.error(ex, "Failed to set enum value.");
			}
		}
		return false;
	}

	private static boolean handlePrimitive(Object obj, String fieldName, Object value) {
		// Check if it's primitive or wrapper or String
		if (!isPrimitive(value)) return false;

		try {
			// Do it via setter method
			String setter = resolveSetter(fieldName);
			Logger.debug("Attempting to set 'primitive' type {} to value {} via setter {}", value.getClass().getSimpleName(), value, setter);
			Reflect.on(obj).call(setter, value);
			return true;
		} catch (Exception ex) {
			Logger.error(ex, "Failed to set 'primitive' value.");
			return false;
		}
	}

	private static Optional<Object> handleComplexType(String type, SequencedMap<String, ?> map) {
		if (type == null) return Optional.empty();

		// Extract args if present
		Object[] args = Optional.ofNullable(map.remove(ARGS_TAG))
			.map(o -> ((List<?>) o).toArray())
			.orElseGet(() -> new Object[0]);

		// Extract steps if present
		List<Step> steps = Optional.ofNullable(map.remove(STEPS_TAG))
			.map(o -> (List<?>) o)
			.map(YamlDeserializer.instance()::parseSteps)
			.orElseGet(List::of);

		Optional<Object> opt;
		if (map.containsKey(FACTORY_TAG)) { // Handle factories/builders
			opt = handleFactory(type, map, args);
		} else { // Standard object instantiation
			Logger.debug("Creating complex type {} with args {}", type, Arrays.toString(args));
			opt = createOpt(type, args);
		}

		if (opt.isEmpty()) return Optional.empty();

		// Extract properties and initialize the object
		Logger.debug("Extracting properties for complex type...");
		Set<Property> properties = map.entrySet().stream()
			.map(e -> {
				String pName = e.getKey();
				String pType = Property.getPropertyType(pName, e.getValue());
				Object value = e.getValue();
				return new Property(pName, pType, value);
			}).collect(Collectors.toSet());
		Logger.trace("Properties: {}", properties);
		initialize(opt.get(), properties);

		// Finally execute steps
		for (Step step : steps) {
			if (opt.isEmpty()) break;
			opt = step.run(opt.get());
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

	private static void handleCollection(Object obj, String fieldName, List<?> values) {
		// For now, collections handling requires a getter in the target object
		// to retrieve the collection.
		Collection<? super Object> collection;
		try {
			String getter = resolveGetter(fieldName);
			Logger.debug("Retrieving collection via getter {}", getter);
			collection = Reflect.on(obj).call(getter).get();
			assert collection != null;
		} catch (AssertionError | ReflectException err) {
			Logger.error(err, "Failed to retrieve collection, skipping...");
			return;
		}
		Logger.trace("Retrieved collection of type {} with size {}", collection.getClass(), collection.size());

		// Elements are added to the retrieved collection one by one
		// This is because we need to check whether the element is
		// a "primitive" type or complex type
		for (Object val : values) {
			switch (val) {
				case SequencedMap<?, ?> m -> {
					Logger.debug("Value {} is complex...", val);
					SequencedMap<String, ?> map = CastUtils.asYamlMap(m);
					if (!map.containsKey(TYPE_TAG)) {
						Logger.error("Type property not found for complex type, skipping...");
						continue;
					}

					handleComplexType((String) map.remove(TYPE_TAG), map).ifPresentOrElse(
						o -> {
							Logger.debug("Adding complex type {}:{} to collection", o.getClass(), o);
							collection.add(o);
						},
						() -> Logger.error("Value not added to collection.")
					);
				}
				case Object o when isPrimitive(o) -> {
					Logger.debug("Adding primitive type {}:{} to collection", o.getClass().getSimpleName(), o);
					collection.add(o);
				}
				default -> Logger.error("Unsupported element type {}, skipping...", val.getClass());
			}
		}
	}

	public static boolean isPrimitive(Object obj) {
		return isPrimitive(obj.getClass());
	}

	public static boolean isPrimitive(Class<?> klass) {
		return klass.isPrimitive() || primitives.contains(klass);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Optional<Enum<?>> isEnum(String value) {
		if (!value.contains(".")) return Optional.empty();
		String[] split = value.split("\\.");
		if (split.length < 2) {
			Logger.trace("YAML value {} is not an enum", value);
			return Optional.empty();
		}

		try {
			String enumClass = split[0];
			String enumConst = split[1];
			Class<?> klass = ClassScanner.findClass(enumClass);
			if (!klass.isEnum()) {
				Logger.trace("Class {} is not an enum");
				return Optional.empty();
			}
			Enum<?> enumVal = Enum.valueOf((Class<? extends Enum>) klass, enumConst);
			return Optional.of(enumVal);
		} catch (ClassNotFoundException | IllegalArgumentException ex) {
			Logger.error("Failed to resolve enum constant {}", value);
			Logger.error(ex);
			return Optional.empty();
		}
	}

	public static String resolveGetter(String fieldName) {
		return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

	public static String resolveSetter(String fieldName) {
		return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}
}