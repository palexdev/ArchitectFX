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


import java.util.Arrays;

/// Represents the invocation to a method. Wraps three pieces of information:
/// 1) The _owner_ as a [String], basically indicates that the method is static, the value is the name of the class
/// (simple or fully-qualified)
/// 2) The methods' _name_
/// 3) The methods' parameters as an array of [Value] objects
public class MethodCall {
    //================================================================================
    // Properties
    //================================================================================
    private String owner;
    private String name;
    private Value<?>[] args;

    //================================================================================
    // Constructors
    //================================================================================
    public MethodCall(String name, Value<?>[] args) {
        this(null, name, args);
    }

    public MethodCall(String owner, String name, Value<?>[] args) {
        this.owner = owner;
        this.name = name;
        this.args = args;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MethodCall{");
        if (owner != null) sb.append("owner=").append(owner).append('\'').append(", ");
        sb.append("name='").append(name).append('\'');
        if (args != null) sb.append(", args=").append(Arrays.toString(args));
        return sb.append('}').toString();
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

    public Value<?>[] getArgs() {
        return args;
    }

    public void setArgs(Value<?>[] args) {
        this.args = args;
    }
}
