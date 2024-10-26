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

package io.github.palexdev.architectfx.backend.yaml;

/// In simple terms: special Strings which map to Java keywords.
///
/// For example, suppose we want to invoke a method which needs the current parsing object as one of its inputs.
/// How can we distinguish between the simple String "this" and the Java keyword "this"?
///
/// This is what this class is for. We are basically reserving some special Strings, in the hope this does not limit the
/// user. When you use `Keyword.CONSTANT` in YAML, the system will automatically detect that we are dealing with a static
/// field of this class and handle the keyword appropriately.
public record Keyword(String name) {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final Keyword THIS = new Keyword("THIS");
}
