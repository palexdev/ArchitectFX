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

// FIXME !Critical! For the love of programming improve this on MaterialFX side
public class SelectableSurface extends MaterialSurface {
    //================================================================================
    // Properties
    //================================================================================
    private final Region owner;
    private double lastOpacity;

    //================================================================================
    // Constructors
    //================================================================================
    public SelectableSurface(Region owner) {
        super(owner);
        this.owner = owner;
    }

    //================================================================================
    // Methods
    //================================================================================
    protected boolean isSelected() {
        return PseudoClasses.SELECTED.isActiveOn(owner);
    }
    
    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public void handleBackground() {
        final double target;
        if (isOwnerDisabled()) {
            target = 0.0;
        } else if (isOwnerPressed() || isSelected()) {
            target = getPressOpacity();
        } else if (isOwnerFocused()) {
            target = getFocusOpacity();
        } else if (isOwnerHover()) {
            target = getHoverOpacity();
        } else {
            target = 0.0;
        }

        if (lastOpacity == target) return;
        if (animated && isAnimateBackground()) {
            animateBackground(target);
        } else {
            getChildren().getFirst().setOpacity(target);
        }
        lastOpacity = target;
    }
}
