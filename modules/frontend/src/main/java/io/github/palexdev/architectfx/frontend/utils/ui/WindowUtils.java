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

package io.github.palexdev.architectfx.frontend.utils.ui;

import io.github.palexdev.mfxcore.base.beans.Size;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class WindowUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private WindowUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static void clampWindowSizes(Size defaultValue) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        defaultValue.setWidth(Math.min(defaultValue.getWidth(), bounds.getWidth() - 50));
        defaultValue.setHeight(Math.min(defaultValue.getHeight(), bounds.getHeight() - 50));
    }
}
