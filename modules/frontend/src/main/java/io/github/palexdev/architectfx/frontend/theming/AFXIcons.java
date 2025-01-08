/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
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

package io.github.palexdev.architectfx.frontend.theming;

import java.util.Arrays;

import io.github.palexdev.mfxresources.fonts.IconDescriptor;

public enum AFXIcons implements IconDescriptor {
    FOLDER_ARROW_DOWN("afx-folder-arrow-down", '\uE900'),
    ;

    private final String description;
    private final char code;

    AFXIcons(String description, char code) {
        this.description = description;
        this.code = code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public char getCode() {
        return code;
    }

    @Override
    public IconDescriptor findByDescription(String description) {
        return Arrays.stream(values())
            .filter(d -> d.description.equals(description))
            .findFirst()
            .orElse(null);
    }

    /**
     * Converts the given icon description/name to its corresponding unicode character.
     *
     * @param desc the icon description/name
     * @return the icon's unicode character
     * @throws IllegalArgumentException if no icon with the given description could be found
     */
    public static char toCode(String desc) {
        return Arrays.stream(values())
            .filter(i -> i.description.equals(desc))
            .findFirst()
            .map(AFXIcons::getCode)
            .orElseThrow(() -> new IllegalArgumentException("Icon description '" + desc + "' is invalid!"));
    }
}
