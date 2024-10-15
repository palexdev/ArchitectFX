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

/// Core class of this system which basically combines the features of [MavenHelper] and [DynamicClassLoader].
///
/// This is the class to use in this system when you want to load a class. This is because classes that come from
/// dependencies are handled by the [DynamicClassLoader] and can't be loaded otherwise.
///
/// Through various `add` methods, you can specify Maven coordinates as inputs (you can also use [MavenHelper#artifact(String, String, String)]),
/// which are downloaded by the [MavenHelper] and stored here as Files in a Set.
///
/// When developing an app that uses this system to load views from YAML documents, such functionality can be considered superfluous.
/// However, it can still be quite useful in some contexts. You can load a view without even adding the dependency to
/// your app, mind-blowing. And of course, this is still an important sub-system in the road to _the new Scene Builder._
public class DependencyManager {
    //================================================================================
    // Properties
    //================================================================================
    private final Set<File> dependencies = new HashSet<>();
    private DynamicClassLoader classLoader = new DynamicClassLoader();

    //================================================================================
    // Methods
    //================================================================================

    /// Tells the [DynamicClassLoader] to load a class with the given **fully qualified** name through
    /// [ClassLoader#loadClass(String)].
    public Class<?> loadClass(String fqName) throws ClassNotFoundException {
        return classLoader.loadClass(fqName);
    }

    /// Downloads the given Maven coordinates as Files and stores them.
    public DependencyManager addDeps(String... artifacts) {
        if (artifacts.length != 0) {
            File[] deps = MavenHelper.downloadFiles(artifacts);
            Collections.addAll(dependencies, deps);
            refresh();
        }
        return this;
    }

    /// Adds the given Files to the dependencies Set.
    public DependencyManager addDeps(File... deps) {
        Collections.addAll(dependencies, deps);
        refresh();
        return this;
    }

    /// Removes all the dependencies.
    public DependencyManager cleanDeps() {
        dependencies.clear();
        refresh();
        return this;
    }

    /// This method is responsible for creating a new [DynamicClassLoader] with all the dependencies in [#dependencies()].
    protected void refresh() {
        classLoader = new DynamicClassLoader();
        classLoader.addJars(dependencies);
    }

    //================================================================================
    // Getters
    //================================================================================
    public Set<File> dependencies() {
        return dependencies;
    }

    public DynamicClassLoader loader() {
        return classLoader;
    }
}