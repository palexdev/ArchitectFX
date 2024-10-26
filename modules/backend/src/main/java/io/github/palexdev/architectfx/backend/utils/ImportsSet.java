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

package io.github.palexdev.architectfx.backend.utils;

import java.util.TreeSet;

/// A specialized [TreeSet] which sort Java imports as follows:
/// fully qualified imports come first, then star imports, otherwise alphabetically.
public class ImportsSet extends TreeSet<String> {

    //================================================================================
    // Constructors
    //================================================================================
    public ImportsSet() {
        super((a, b) -> {
            // Ensure fully qualified imports (non-star) come before star imports
            boolean aStar = a.endsWith(".*");
            boolean bStar = b.endsWith(".*");
            if (aStar && !bStar) return 1;
            if (!aStar && bStar) return -1;
            return a.compareTo(b);  // Lexicographical order otherwise
        });
    }
}