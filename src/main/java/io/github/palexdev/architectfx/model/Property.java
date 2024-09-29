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

import io.github.palexdev.architectfx.enums.Type;
import org.tinylog.Logger;

public record Property(String name, Type type, Object value) {

    //================================================================================
    // Static Methods
    //================================================================================
    public static Property of(String name, Type type, Object value) {
        return new Property(name, type, value);
    }

    public static Type getPropertyType(String name, Object value) {
        Logger.trace("Determining property type for Name:{} Value:{}", name, value);
        Type type = Type.getType(value);
        Logger.trace("Found type {} for property {}", type, name);
        return type;
    }
}
