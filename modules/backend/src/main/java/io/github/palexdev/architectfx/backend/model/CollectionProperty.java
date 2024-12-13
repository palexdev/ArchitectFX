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


import io.github.palexdev.architectfx.backend.enums.CollectionHandleStrategy;
import io.github.palexdev.architectfx.backend.model.types.Value;
import io.github.palexdev.architectfx.backend.model.types.Value.CollectionValue;

/// Specialization of [ObjProperty] for properties which are collections. Wraps an additional piece of information
/// which is a constant of type [CollectionHandleStrategy]. It is used to give the system a clue on how to handle the
/// collection. For example one may want to just append elements, or clear before doing so.
public class CollectionProperty extends ObjProperty {
    //================================================================================
    // Properties
    //================================================================================
    private CollectionHandleStrategy strategy;

    //================================================================================
    // Constructors
    //================================================================================
    public CollectionProperty(String name, CollectionHandleStrategy strategy) {
        super(name);
        this.strategy = strategy;
    }

    public CollectionProperty(String name, CollectionValue value, CollectionHandleStrategy strategy) {
        super(name, value);
        this.strategy = strategy;
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public CollectionHandleStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(CollectionHandleStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public CollectionValue getValue() {
        return (CollectionValue) super.getValue();
    }

    @Override
    public void setValue(Value<?> value) {
        if (!(value instanceof CollectionValue))
            throw new IllegalArgumentException("Expected value of type collection, got: " + value.getClass());
        super.setValue(value);
    }
}
