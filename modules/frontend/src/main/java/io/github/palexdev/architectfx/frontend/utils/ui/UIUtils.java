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

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.mfxcomponents.window.MFXPlainContent;
import io.github.palexdev.mfxcomponents.window.popups.MFXTooltip;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxeffects.animations.motion.M3Motion;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.util.Duration;

public class UIUtils {

    //================================================================================
    // Constructors
    //================================================================================
    private UIUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static Size clampWindowSizes(Size size) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        return Size.of(
            Math.min(size.getWidth(), bounds.getWidth() - 50),
            Math.min(size.getHeight(), bounds.getHeight() - 50)
        );
    }

    public static MFXTooltip installTooltip(Node owner, String text) {
        return installTooltip(owner, text, Pos.BOTTOM_CENTER);
    }

    public static MFXTooltip installTooltip(Node owner, String text, Pos anchor) {
        MFXTooltip tooltip = new MFXTooltip(owner);
        tooltip.setContent(new MFXPlainContent(text));
        tooltip.setInDelay(M3Motion.EXTRA_LONG4);
        tooltip.setOutDelay(Duration.ZERO);
        tooltip.setAnchor(anchor);
        return tooltip.install();
    }

    public static void debugTheme(Parent parent, String theme) {
        CSSFX.start(parent);
        parent.getStylesheets().add(Resources.load(theme));
    }
}
