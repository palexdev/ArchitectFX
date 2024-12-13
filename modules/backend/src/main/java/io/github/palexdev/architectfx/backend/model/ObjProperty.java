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


import io.github.palexdev.architectfx.backend.model.types.Value;

/// Represent a property of a [UIObj]. Simply wraps two pieces of information:
/// 1) The `name` of the property which must correspond to a field in the target object.
/// 2) The `value` to which set the property, expressed as a [Value] object.
public class ObjProperty {
    //================================================================================
    // Properties
    //================================================================================
    private final String name;
    private Value<?> value;

    //================================================================================
    // Constructors
    //================================================================================
    public ObjProperty(String name) {
        this(name, null);
    }

    public ObjProperty(String name, Value<?> value) {
        this.name = name;
        this.value = value;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        return "NodeProperty{" +
               "name='" + name + '\'' +
               ", value=" + value +
               '}';
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public String getName() {
        return name;
    }

    public Value<?> getValue() {
        return value;
    }

    public void setValue(Value<?> value) {
        this.value = value;
    }
}
