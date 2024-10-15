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

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;

/// Facade class which allows to download Maven artifacts through [Shrinkwrap](https://github.com/shrinkwrap/resolver).
/// To be precise, this awesome third-party library also handles dependencies and transitivity.
///
/// @see DependencyManager
public class MavenHelper {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final MavenResolverSystem resolver = Maven.resolver();

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

    /// Downloads a series of Maven artifacts given their coordinates and returns the retrieved artifacts and dependencies
    /// as files. Transitivity is enabled.
    public static File[] downloadFiles(String... artifacts) {
        return resolver.resolve(artifacts)
            .withTransitivity()
            .asFile();
    }

    /// Downloads a series of Maven artifacts given their coordinates and returns the retrieved artifacts and dependencies
    /// as [MavenResolvedArtifact]. Transitivity is enabled.
    public static MavenResolvedArtifact[] downloadArtifacts(String... artifacts) {
        return resolver.resolve(artifacts)
            .withTransitivity()
            .asResolvedArtifact();
    }
}