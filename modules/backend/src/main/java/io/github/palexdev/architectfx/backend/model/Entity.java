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

package io.github.palexdev.architectfx.backend.model;

import java.util.*;

import io.github.palexdev.architectfx.backend.enums.Type;
import io.github.palexdev.architectfx.backend.yaml.YamlDeserializer;
import io.github.palexdev.architectfx.backend.yaml.YamlLoader;

/// Record that represents a structured node in the YAML document hierarchy. An entity may or may not contain other
/// entities, its `children`.
///
/// Complex values, as described by [Type#COMPLEX], are not considered entities (they are not part of the tree hierarchy)
/// and can appear in any of the entity's properties.
///
/// Any entity stores the following list of information:
/// 1) The parent entity, `null` in case it's the root entity
/// 2) The type, which is just the key String parsed from YAML
/// 3) The instance object. Every entity is created only once its type has been instantiated. More on how the load
/// process works here [YamlLoader] and here [YamlDeserializer]
/// 4) Its properties stored in a map of type `[String -> Property]`, where keys are the property's name, and the values
/// are records which represent the property
/// 5) Its `children` as a List of entities
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

    /// Convenience method to add a property to the properties map.
    public void addProperty(Property property) {
        properties.put(property.name(), property);
    }

    /// @return an `Optional` that may or may not wrap a property for the given name
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
