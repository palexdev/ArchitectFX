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

package io.github.palexdev.architectfx.deps;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.joor.Reflect;
import org.tinylog.Logger;

import io.github.palexdev.architectfx.utils.ReflectionUtils;

public class DependencyManager {
	//================================================================================
	// Singleton
	//================================================================================
	private static final DependencyManager instance = new DependencyManager();

	public static DependencyManager instance() {
		return instance;
	}

	//================================================================================
	// Properties
	//================================================================================
	private final Set<File> dependencies = new HashSet<>();
	private final MavenHelper mavenHelper = new MavenHelper();
	private DynamicClassLoader classLoader = new DynamicClassLoader();

	//================================================================================
	// Constructors
	//================================================================================
	private DependencyManager() {
	}

	//================================================================================
	// Methods
	//================================================================================
	public <T> T create(String className, Object... args) {
		try {
			Class<?> klass = ReflectionUtils.findClass(className);
			return create(klass, args);
		} catch (ClassNotFoundException | IllegalStateException ex) {
			Logger.error(ex, "Failed to create class {}", className);
			return null;
		}
	}

	public <T> T create(Class<?> klass, Object... args) {
		try {
			Logger.trace("Attempting to create class {} with args: {}", klass.getName(), Arrays.toString(args));
			return Reflect.onClass(klass.getName(), classLoader)
				.create(args)
				.get();
		} catch (Exception ex) {
			Logger.error("Failed to create class {} because: {}", klass.getName(), ex.getMessage());
			return null;
		}
	}

	public <T> Optional<T> createOpt(String className, Object... args) {
		return Optional.ofNullable(create(className, args));
	}

	public <T> Optional<T> createOpt(Class<?> klass, Object... args) {
		return Optional.ofNullable(create(klass, args));
	}

	public <T> T invokeFactory(String factoryName, Object... args) {
		int lastDot = factoryName.lastIndexOf(".");
		String className = factoryName.substring(0, lastDot);
		String method = factoryName.substring(lastDot + 1);
		try {
			Logger.trace(
				"Attempting to call factory {} with args: {}\n Class: {}\n Static Method: {}",
				 factoryName, Arrays.toString(args), className, method
			);
			Class<?> klass = ReflectionUtils.findClass(className);
			return Reflect.onClass(klass.getName(), classLoader)
				.call(method, args)
				.get();
		} catch (Exception ex) {
			Logger.error("Failed to invoke factory {} because: {}", factoryName, ex.getMessage());
			return null;
		}
	}

	public <T> Optional<T> invokeFactoryOpt(String factoryName, Object... args) {
		return Optional.ofNullable(invokeFactory(factoryName, args));
	}

	public Class<?> loadClass(String fqName) {
		try {
			return classLoader.loadClass(fqName);
		} catch (ClassNotFoundException ex) {
			Logger.error(ex, "Failed to load class {}", fqName);
			return null;
		}
	}

	public DependencyManager addDeps(String... artifacts) {
		if (artifacts.length != 0) {
			File[] deps = mavenHelper.downloadFiles(artifacts);
			Collections.addAll(dependencies, deps);
		}
		return this;
	}

	public DependencyManager addDeps(File... deps) {
		Collections.addAll(dependencies, deps);
		return this;
	}

	public DependencyManager cleanDeps() {
		dependencies.clear();
		return this;
	}

	public DependencyManager refresh() {
		try {
			classLoader.close();
		} catch (Exception ex) {
			Logger.warn(ex, "Failed to dispose old class loader");
		}
		classLoader = new DynamicClassLoader();
		classLoader.addJars(dependencies);
		return this;
	}

	//================================================================================
	// Getters
	//================================================================================
	public Set<File> getDependencies() {
		return Collections.unmodifiableSet(dependencies);
	}
}