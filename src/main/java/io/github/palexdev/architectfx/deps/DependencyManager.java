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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.tinylog.Logger;

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
    private DependencyManager() {}

    //================================================================================
    // Methods
    //================================================================================

    public Class<?> loadClass(String fqName) throws ClassNotFoundException {
        return classLoader.loadClass(fqName);
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
    public Set<File> dependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public DynamicClassLoader loader() {
        return classLoader;
    }
}