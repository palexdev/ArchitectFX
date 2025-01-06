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

package io.github.palexdev.architectfx.frontend.components.dialogs;

import java.util.List;
import java.util.Optional;

import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.Dialog;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXButton;
import io.github.palexdev.mfxcomponents.controls.progress.MFXProgressIndicator;
import io.github.palexdev.mfxcomponents.controls.progress.ProgressDisplayMode;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

public class ProgressDialog extends Dialog {
    //================================================================================
    // Properties
    //================================================================================
    private final ObjectProperty<Progress> progress = new SimpleObjectProperty<>(Progress.INDETERMINATE) {
        @Override
        protected void invalidated() {
            Progress progress = get();
            if (progress != null &&
                (Progress.CANCELED == progress || progress.isDone())
            ) Platform.runLater(ProgressDialog.this::hide);
        }
    };
    private Runnable onCancel = () -> {};

    //================================================================================
    // Constructors
    //================================================================================
    public ProgressDialog() {
        super();
        setContent(buildContent());
    }

    //================================================================================
    // Methods
    //================================================================================
    protected void cancel() {
        Optional.ofNullable(onCancel)
            .ifPresent(Runnable::run);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected Node buildContent() {
        MFXProgressIndicator indicator = new MFXProgressIndicator();
        indicator.setDisplayMode(ProgressDisplayMode.CIRCULAR);
        indicator.progressProperty().bind(progress.map(Progress::progress));

        MFXButton cancel = new MFXButton("Cancel").outlined();
        cancel.setOnAction(e -> cancel());
        cancel.getStyleClass().add("warn");

        Label descLabel = new Label();
        descLabel.textProperty().bind(progress.map(Progress::description));

        return new Box(
            Box.Direction.COLUMN,
            indicator,
            descLabel,
            cancel
        );
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

    public ObjectProperty<Progress> progressProperty() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress.set(progress);
    }

    public Runnable getOnCancel() {
        return onCancel;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }
}
