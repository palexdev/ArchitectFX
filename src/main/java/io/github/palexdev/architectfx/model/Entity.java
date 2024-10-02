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

public record Entity(
    Entity parent,
    String type,
    Object instance,
    SequencedMap<String, Property> properties,
    List<Entity> children
) {
    //================================================================================
    // Constructors
    //================================================================================
    public Entity(Entity parent, String type, Object instance) {
        this(parent, type, instance, new LinkedHashMap<>(), new ArrayList<>());
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(type, entity.type) &&
               Objects.equals(instance, entity.instance) &&
               Objects.equals(children, entity.children) &&
               Objects.equals(properties, entity.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, instance, properties, children);
    }

    @Override
    public String toString() {
        return "Node{" +
               "type='" + type + '\'' +
               ", n.properties=" + properties.size() +
               ", n.children=" + children.size() +
               '}';
    }
}
