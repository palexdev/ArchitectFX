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

import java.util.*;

import io.github.palexdev.architectfx.backend.utils.ImportsSet;
import javafx.scene.Parent;

/// Record which represents a YAML document.
///
/// Here's what it contains:
/// 1) The root of the three structure
/// 2) The controller if any was specified
/// 3) The dependencies
/// 4) The imports
public record Document(
    Entity root,
    Object controller,
    Set<String> dependencies,
    Set<String> imports
) {

    //================================================================================
    // Constructors
    //================================================================================
    public Document(Entity root, Object controller) {
        this(root, controller, new LinkedHashSet<>(), new ImportsSet());
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Convenience method to retrieve the root node from the entity.
    public Parent rootNode() {
        return (Parent) root.instance();
    }
}
