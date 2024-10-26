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

import io.github.palexdev.architectfx.backend.enums.OSType;

public class OSUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    private static OSType osType;

    //================================================================================
    // Constructors
    //================================================================================
    private OSUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================

    /// @return whether [#os()] is not [OSType#Other]
    public static boolean isSupportedPlatform() {
        return os() != OSType.Other;
    }

    /// @return the OS on which the app is running represented by the enum [OSType]. The result is cached, subsequent
    /// calls will be faster.
    public static OSType os() {
        if (osType == null) {
            String name = System.getProperty("os.name");
            osType = switch (name) {
                case String s when s.contains("win") || s.contains("Win") -> OSType.Windows;
                case String s when s.contains("nix") || s.contains("nux") -> OSType.Linux;
                case String s when s.contains("mac") || s.contains("Mac") -> OSType.MacOS;
                default -> OSType.Other;
            };
        }
        return osType;
    }
}