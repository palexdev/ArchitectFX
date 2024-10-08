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
/// Every time the dependencies change, one must call the [#refresh(boolean)] method to create a new [DynamicClassLoader]
/// with the updated dependencies.
///
/// When developing an app that uses this system to load views from YAML documents, such functionality can be considered superfluous.
/// However, it can still be quite useful in some contexts. You can load a view without even adding the dependency to
/// you app, mind-blowing. And of course, this is still an important sub-system in the road to _the new Scene Builder._
public class DependencyManager {
    //================================================================================
    // Properties
    //================================================================================
    private final Set<File> dependencies = new HashSet<>();
    private final MavenHelper mavenHelper = new MavenHelper();
    private DynamicClassLoader classLoader = new DynamicClassLoader();
    private boolean needsRefresh = false;

    //================================================================================
    // Methods
    //================================================================================

    /// Tells the [DynamicClassLoader] to load a class with the given **fully qualified** name through
    /// [ClassLoader#loadClass(String)].
    public Class<?> loadClass(String fqName) throws ClassNotFoundException {
        return classLoader.loadClass(fqName);
    }

    /// Downloads the given Maven coordinates as Files and stores them.
    ///
    /// After such operation, the [#needsRefresh] flag is going to be true, which means that [#refresh(boolean)] must be
    /// called for the changes to take effect.
    public DependencyManager addDeps(String... artifacts) {
        if (artifacts.length != 0) {
            File[] deps = mavenHelper.downloadFiles(artifacts);
            Collections.addAll(dependencies, deps);
            needsRefresh = true;
        }
        return this;
    }

    /// Adds the given Files to the dependencies Set.
    ///
    /// After such operation, the [#needsRefresh] flag is going to be true, which means that [#refresh(boolean)] must be
    /// called for the changes to take effect.
    public DependencyManager addDeps(File... deps) {
        Collections.addAll(dependencies, deps);
        needsRefresh = true;
        return this;
    }

    /// Removes all the dependencies.
    ///
    /// After such operation, the [#needsRefresh] flag is going to be true, which means that [#refresh(boolean)] must be
    /// called for the changes to take effect.
    public DependencyManager cleanDeps() {
        dependencies.clear();
        needsRefresh = true;
        return this;
    }

    /// This method is responsible for creating a new [DynamicClassLoader] when:
    /// 1) The [#needsRefresh] flag is true
    /// 2) Or if the `force` parameter is true
    ///
    /// At the end, the [#needsRefresh] flag is reset to false.
    public DependencyManager refresh(boolean force) {
        if (needsRefresh || force) {
            classLoader = new DynamicClassLoader();
            classLoader.addJars(dependencies);
        }
        needsRefresh = false;
        return this;
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

    public boolean needsRefresh() {
        return needsRefresh;
    }
}