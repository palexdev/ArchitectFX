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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.joor.Reflect;
import org.joor.ReflectException;
import org.tinylog.Logger;

import io.github.classgraph.ClassInfoList;
import io.github.palexdev.architectfx.deps.DependencyManager;
import io.github.palexdev.architectfx.model.Property;
import io.github.palexdev.architectfx.utils.ClassScanner.ScanScope;

public class ReflectionUtils {
	//================================================================================
	// Static Properties
	//================================================================================
	private static final Set<String> imports = new ImportsSet();
	private static final Map<String, Class<?>> searchCache = new HashMap<>();
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

	public static Class<?> findClass(String className) throws ClassNotFoundException {
		// Check if it's a fully qualified name
		// (naive approach, contains dot)
		// In such case no need to cache
		if (className.contains(".")) {
			Class<?> klass = DependencyManager.instance().loadClass(className);
			if (klass != null) return klass;
			throw new ClassNotFoundException("Class not found: " + className);
		}

		// Simple names handling
		// Check cache first
		if (searchCache.containsKey(className)) return searchCache.get(className);

		// Then try with imports and add to cache
		for (String imp : imports) {
			try {
				Class<?> klass = switch(imp) {
					case String s when s.endsWith(className) -> Class.forName(s);
					case String s when s.endsWith("*") -> {
						String pkg = s.substring(0, s.lastIndexOf('.'));
						yield Class.forName(pkg + "." + className);
					}
					default -> null;
				};

				if (klass != null) {
					searchCache.put(className, klass);
					return klass;
				}
			} catch (ClassNotFoundException ex) {
				Logger.trace("Invalid name or class not found: {}", ex.getMessage());
			}
		}

		// Last resort, use ClassGraph
		Logger.warn("Resorting to ClassGraph to find class {}, this may take a while for the first scan...", className);
		ClassInfoList results = ClassScanner.searchClasses(className, ScanScope.DEPS);
		if (results.isEmpty()) throw new ClassNotFoundException("Class not found: " + className);
		if (results.size() > 1) throw new IllegalStateException(
			"More than one class for name %s have been found: %s".formatted(className, results.toArray())
		);

		String fqName = results.getFirst().getName();
		Class<?> klass = DependencyManager.instance().loadClass(fqName);
		if (klass == null)
			throw new ClassNotFoundException("Failed to load class: " + fqName);
		Logger.trace("Found class: {}", fqName);
		searchCache.put(className, klass);
		return klass;
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
					() -> {
						Logger.error("Could not set complex object {}, skipping...");
					}
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

	@SuppressWarnings({"rawtypes","unchecked"})
	private static boolean handleEnum(Object obj, String fieldName, Object value) {
		try {
			// Get enum class
			String getter = resolveGetter(fieldName);
			Class<?> retType = obj.getClass().getMethod(getter).getReturnType();
			if (!retType.isEnum()) {
				Logger.trace("Type {} is not an enum.", retType);
				return false;
			}

			// Do it via setter method
			String setter = resolveSetter(fieldName);
			Logger.debug("Attempting to set enum value {} via setter {}", value, setter);
			Reflect.on(obj).call(setter, Enum.valueOf((Class<? extends Enum>) retType, (String) value));
			return true;
		} catch (Exception ex) {
			Logger.error(ex, "Failed to set enum value.");
			return false;
		}
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
		Object[] args = new Object[0];
		if (map.containsKey("args")) args = ((List<?>) map.remove("args")).toArray();
		Logger.debug("Creating complex type {} with args {}", type, Arrays.toString(args));
		Optional<Object> opt = DependencyManager.instance().createOpt(type, args);
		if (opt.isEmpty()) return Optional.empty();

		// Exctract properties and initialize the object
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
		return opt;
	}

	private static void handleCollection(Object obj, String fieldName, List<?> values) {
		// For now collections handling requires a getter in the target object
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
					if (!map.containsKey("type")) {
						Logger.error("Type property not found for complex type, skipping...");
						continue;
					}

					handleComplexType((String) map.remove("type"), map).ifPresentOrElse(
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

	public static String resolveGetter(String fieldName) {
		return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

	public static String resolveSetter(String fieldName) {
		return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

	public static void setImports(Collection<String> imports) {
		ReflectionUtils.imports.clear();
		ReflectionUtils.imports.addAll(imports);
	}
}