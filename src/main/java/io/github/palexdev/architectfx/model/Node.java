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

import java.util.*;

// TODO think about another name
public class Node {
    //================================================================================
    // Properties
    //================================================================================
    private final String type;
    private final SequencedMap<String, Property> properties = new LinkedHashMap<>();
    private final List<Node> children = new ArrayList<>();

    //================================================================================
    // Constructors
    //================================================================================
    public Node(String type) {
        this.type = type;
    }

    //================================================================================
    // Methods
    //================================================================================

    public void addProperty(Property property) {
        properties.put(property.name(), property);
    }

    public Optional<Property> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    public String toString() {
        return "Node{" +
            "type='" + type + '\'' +
            ", n.properties=" + properties.size() +
            ", n.children=" + children.size() +
            '}';
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getType() {
        return type;
    }

    public SequencedMap<String, Property> getProperties() {
        return properties;
    }

    public List<Node> getChildren() {
        return children;
    }
}
