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

package io.github.palexdev.architectfx.frontend.components;

import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public interface Scrimmable {
    void addScrim(Rectangle scrim);

    void removeScrim(Rectangle scrim);

    default void apply(Rectangle scrim, double opacity) {
        scrim.setVisible(false);
        addScrim(scrim);
        Parent parent = scrim.getParent();
        scrim.widthProperty().bind(parent.layoutBoundsProperty().map(Bounds::getWidth));
        scrim.heightProperty().bind(parent.layoutBoundsProperty().map(Bounds::getHeight));
        scrim.setFill(Color.rgb(0, 0, 0, opacity));
        scrim.setBlendMode(BlendMode.SRC_ATOP);
        scrim.setVisible(true);
    }
}
