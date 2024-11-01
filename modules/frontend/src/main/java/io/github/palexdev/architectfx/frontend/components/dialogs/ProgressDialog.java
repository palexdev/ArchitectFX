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

package io.github.palexdev.architectfx.frontend.components.dialogs;

import java.util.List;

import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.Dialog;
import io.github.palexdev.architectfx.frontend.utils.ProgressProperty;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXButton;
import io.github.palexdev.mfxcomponents.controls.progress.MFXProgressIndicator;
import io.github.palexdev.mfxcomponents.controls.progress.ProgressDisplayMode;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ProgressDialog extends Dialog {
    //================================================================================
    // Properties
    //================================================================================
    private ProgressProperty progress;

    private MFXProgressIndicator indicator;
    private Label desc;

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected Node buildContent() {
        indicator = new MFXProgressIndicator();
        indicator.setDisplayMode(ProgressDisplayMode.CIRCULAR);
        indicator.setProgress(progressProperty().getProgress());

        MFXButton cancel = new MFXButton("Cancel").outlined();
        cancel.setOnAction(e -> progressProperty().set(Progress.CANCELED));
        cancel.getStyleClass().add("warning");

        desc = new Label();
        desc.setText(progressProperty().getDescription());

        VBox container = new VBox(24.0, indicator, desc, cancel);
        container.getStyleClass().add("box");
        return container;
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("mfx-popup", "progress-dialog");
    }

    @Override
    protected void dispose() {
        progressProperty().unbind();
        super.dispose();
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public Progress getProgress() {
        return progress.get();
    }

    public ProgressProperty progressProperty() {
        if (progress == null) progress = new ProgressProperty() {
            @Override
            protected void invalidated() {
                Progress current = get();

                Platform.runLater(() -> {
                    indicator.setProgress(current.progress());
                    desc.setText(current.description());
                });

                if (current == Progress.CANCELED || current.progress() == 1.0) {
                    Platform.runLater(ProgressDialog.this::hide);
                }
            }
        };
        return progress;
    }
}
