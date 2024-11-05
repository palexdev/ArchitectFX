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

package io.github.palexdev.architectfx.frontend.components;

import io.github.palexdev.mfxcomponents.controls.MaterialSurface;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import javafx.scene.layout.Region;

public class SelectableSurface extends MaterialSurface {

    //================================================================================
    // Constructors
    //================================================================================
    public SelectableSurface(Region owner) {
        super(owner);
        /*
         * TODO: States can be stored in a PriorityQueue for easier custom states
         */
        getStates().add(1, State.of(
            PseudoClasses.SELECTED::isActiveOn,
            MaterialSurface::getPressedOpacity
        ));
    }
}
