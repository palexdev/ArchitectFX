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

package io.github.palexdev.architectfx.examples.common;

import io.github.palexdev.architectfx.backend.model.Initializable;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.fx.StageUtils;
import io.github.palexdev.mfxcore.utils.resize.StageResizer;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class HeaderController implements Initializable {
    private Stage mainWindow;
    private Pane rootPane;

    private Region separator;
    private MFXFontIcon aotIcon;
    private MFXFontIcon minIcon;
    private MFXFontIcon maxIcon;
    private MFXFontIcon clsIcon;

    private Size prevWindowSize = Size.of(0, 0);

    @Override
    public void initialize() {
        // Window handling
        StageUtils.makeDraggable(mainWindow, separator);
        StageResizer resizer = new StageResizer(rootPane, mainWindow);
        resizer.setMinWidthFunction(r -> rootPane.getMinWidth());
        resizer.setMinHeightFunction(r -> rootPane.getMinHeight());
        resizer.makeResizable();

        // Handle always on top
        aotIcon.setOnMouseClicked(e -> mainWindow.setAlwaysOnTop(!mainWindow.isAlwaysOnTop()));
        When.onInvalidated(mainWindow.alwaysOnTopProperty())
            .then(v -> PseudoClasses.setOn(aotIcon, "aot", v))
            .executeNow()
            .listen();

        // Handle minimize
        minIcon.setOnMouseClicked(e -> mainWindow.setIconified(true));

        // Handle maximize
        maxIcon.setOnMouseClicked(e -> {
            if (!mainWindow.isMaximized()) {
                prevWindowSize = Size.of(
                    rootPane.getWidth(),
                    rootPane.getHeight()
                );
                mainWindow.setMaximized(true);
            } else {
                mainWindow.setMaximized(false);
                mainWindow.setWidth(prevWindowSize.getWidth());
                mainWindow.setHeight(prevWindowSize.getHeight());
                mainWindow.centerOnScreen();
            }
        });

        // Handle close
        clsIcon.setOnMouseClicked(e -> mainWindow.hide());
    }
}
