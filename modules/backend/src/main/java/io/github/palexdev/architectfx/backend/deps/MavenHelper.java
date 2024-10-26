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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.mccue.jresolve.Cache;
import dev.mccue.jresolve.Dependency;
import dev.mccue.jresolve.Library;
import dev.mccue.jresolve.Resolve;
import io.github.palexdev.architectfx.backend.utils.Async;

/// Facade class which allows downloading Maven artifacts through [JResolve](https://github.com/palexdev/jresolve).
///
/// @see DependencyManager
public class MavenHelper {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final Path MAVEN_LOCAL = Path.of(System.getProperty("user.home"), ".m2/repository");

    //================================================================================
    // Constructors
    //================================================================================
    private MavenHelper() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// Convenience method to combine a Maven artifact's group, name and version into a single string.
    public static String artifact(String group, String name, String version) {
        return group + ":" + name + ":" + version;
    }

    /// Convenience method to create a collection of [Dependency] objects given their Maven coordinates.
    ///
    /// Automatically excludes JavaFX!
    @SuppressWarnings("removal")
    public static List<Dependency> dependencies(String... coordinates) {
        List<Dependency> dependencies = new ArrayList<>();
        for (String coordinate : coordinates) {
            Dependency dependency = Dependency.mavenCentral(coordinate)
                .withExclusions("org.openjfx:*");
            dependencies.add(dependency);
        }
        return dependencies;
    }

    /// Downloads a series of Maven artifacts given their coordinates and returns the retrieved artifacts and dependencies
    /// as files.
    public static Path[] downloadFiles(String... coordinates) {
        return downloadLibraries(coordinates)
            .values()
            .toArray(Path[]::new);
    }

    /// Downloads a series of Maven artifacts given their coordinates and returns the retrieved artifacts and dependencies.
    public static Map<Library, Path> downloadLibraries(String... coordinates) {
        return new Resolve()
            .withCache(Cache.standard(MAVEN_LOCAL))
            .addDependencies(dependencies(coordinates))
            .withExecutorService(Async.executor())
            .fetch()
            .run()
            .libraries();
    }
}