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

package io.github.palexdev.architectfx.backend.model.types;


import java.util.Objects;

/// This class represents the reference to a field in a class/object.
/// Wraps two values:
/// 1) The owner class as a [String] if the field is static
/// 2) The field's name
public class FieldRef {
    //================================================================================
    // Properties
    //================================================================================
    private String owner;
    private String name;

    //================================================================================
    // Constructors
    //================================================================================
    public FieldRef(String name) {
        this.name = name;
    }

    public FieldRef(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    //================================================================================
    // Methods
    //================================================================================

    /// Convenience method to check whether [#getOwner()] is `null`.
    public boolean isStatic() {
        return getOwner() != null;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FieldRef{");
        if (owner != null) sb.append("owner='").append(owner).append('\'').append(", ");
        sb.append("name='").append(name).append('\'').append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FieldRef fieldRef = (FieldRef) o;
        return Objects.equals(owner, fieldRef.owner) && Objects.equals(name, fieldRef.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
