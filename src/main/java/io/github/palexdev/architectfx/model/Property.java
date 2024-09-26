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

import org.tinylog.Logger;

import java.util.SequencedMap;

import static io.github.palexdev.architectfx.yaml.YamlFormatSpecs.TYPE_TAG;

public class Property {
    //================================================================================
    // Properties
    //================================================================================
    private final String name;
    private final String type;
    private final Object value;

    //================================================================================
    // Constructors
    //================================================================================
    public Property(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static String getPropertyType(String name, Object property) {
        Logger.trace("Determining property type for Name:{} Value:{}", name, property);
        if (property instanceof SequencedMap<?,?> m) {
            Logger.trace("Type is complex...");
            Object type = m.remove(TYPE_TAG);
            if (type instanceof String s) {
                Logger.trace("Found type {} for property {}", s, name);
                return s;
            } else {
                Logger.error("Unable to determine type for Name:{} Value:{}", name, property);
                throw new IllegalArgumentException(
                    "Unexpected type %s for property %s".formatted(name,  property)
                );
            }
        }
        String className = property.getClass().getSimpleName();
        Logger.trace("Found type {} for property {}", className, name);
        return className;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        return "Property{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", value=" + value +
            '}';
    }

    //================================================================================
    // Getters
    //================================================================================
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
