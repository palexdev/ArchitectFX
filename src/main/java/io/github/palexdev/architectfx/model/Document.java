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

package io.github.palexdev.architectfx.model;

import java.util.HashSet;
import java.util.Set;

public class Document {
    //================================================================================
    // Properties
    //================================================================================
    private final Node root;
    private final String controller;
    private final Set<String> dependencies = new HashSet<>();
    private final Set<String> imports = new HashSet<>();

    //================================================================================
    // Constructors
    //================================================================================
    public Document(Node root, String controller) {
        this.root = root;
        this.controller = controller;
    }

    //================================================================================
    // Getters
    //================================================================================
    public Node getRoot() {
        return root;
    }

    public String getController() {
        return controller;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getImports() {
        return imports;
    }
}
