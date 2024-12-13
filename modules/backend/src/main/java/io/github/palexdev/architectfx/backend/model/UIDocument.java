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

package io.github.palexdev.architectfx.backend.model;


import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import io.github.palexdev.architectfx.backend.deps.DependencyManager;
import io.github.palexdev.architectfx.backend.utils.ImportsSet;

/// Represents a UI graph document, wrapping all the necessary pieces of information to properly load it:
/// - The `location` of the document as a [URI], this is important for stuff like resources' resolution. If the location
/// is not set, some mechanisms (such as the aforementioned resources' resolution) may not be available
/// - Third-party 'dependencies', a series Maven coordinates as strings. These may be downloaded and managed through
/// the [DependencyManager]
/// - Java `imports` which help the system understand what object type you are referring to in the document. They can
/// be omitted (given that there are no other objects with the same name on the classpath) as the system is capable of
/// resolving simple names to a class, BUT, keep in mind that this process is costly and may reduce performance significantly
/// - The `root` [UIObj] from which the UI graph starts
/// - A `controller` in which [UIObj] marked by a _controllerId_ may be injected by the system
public class UIDocument {
    //================================================================================
    // Properties
    //================================================================================
    private final URI location;
    private final Set<String> dependencies = new HashSet<>();
    private final Set<String> imports = new ImportsSet();
    private final UIObj root;
    private UIObj controller; // TODO this could be changed to a supplier to support both controller in the document and by "factory" in one go

    //================================================================================
    // Constructors
    //================================================================================
    public UIDocument(URI location, UIObj root) {
        this.location = location;
        this.root = root;
        root.root = true;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public URI getLocation() {
        return location;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getImports() {
        return imports;
    }

    public UIObj getRoot() {
        return root;
    }

    public UIObj getController() {
        return controller;
    }

    public void setController(UIObj controller) {
        this.controller = controller;
    }
}
