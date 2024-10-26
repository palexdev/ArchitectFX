/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
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

package io.github.palexdev.architectfx.backend.deps;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;

import org.tinylog.Logger;

/// A simple extension of [URLClassLoader] which by default extends the system class loader
/// ([#getSystemClassLoader()]) and allows adding JARs even at runtime. This way you can load classes that are not even
/// part of your project/environment.
///
/// @see DependencyManager
@SuppressWarnings("resource")
public class DynamicClassLoader extends URLClassLoader {

    //================================================================================
    // Constructors
    //================================================================================
    public DynamicClassLoader() {
        this(getSystemClassLoader());
    }

    public DynamicClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Delegates to [#addJar(Path)]
    public DynamicClassLoader addJars(Collection<Path> files) {
        for (Path p : files) {
            addJar(p);
        }
        return this;
    }

    /// Delegates to [#addJar(Path)]
    public DynamicClassLoader addJars(Path... files) {
        for (Path p : files) {
            addJar(p);
        }
        return this;
    }

    /// Expects a JAR file as input, which is converted to a [URL] and added to the class loader through [#addURL(URL)].
    public DynamicClassLoader addJar(Path file) {
        try {
            URL jar = file.toUri().toURL();
            Logger.info("Loading dependency: {}", jar);
            super.addURL(jar);
        } catch (Exception ex) {
            Logger.error(ex, "Failed to add file {} to dynamic class loader", file);
        }
        return this;
    }
}