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

package io.github.palexdev.architectfx.frontend.utils;

import java.util.Optional;

import io.github.palexdev.architectfx.backend.utils.Progress;
import io.github.palexdev.mfxcore.base.properties.resettable.ResettableObjectProperty;

public class ProgressProperty extends ResettableObjectProperty<Progress> {
    //================================================================================
    // Constructors
    //================================================================================
    public ProgressProperty() {
        super(Progress.INDETERMINATE, Progress.INDETERMINATE);
    }

    public ProgressProperty(Progress initialValue) {
        super(initialValue, initialValue);
    }

    public ProgressProperty(Progress initialValue, Progress defaultValue) {
        super(initialValue, defaultValue);
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public String getDescription() {
        return Optional.ofNullable(get()).map(Progress::description).orElse("");
    }

    public void setDescription(String description) {
        Progress newProgress = Optional.ofNullable(get())
            .map(p -> new Progress(description, p.progress()))
            .orElseGet(() -> new Progress(description, 0f));
        set(newProgress);
    }

    public double getProgress() {
        return Optional.ofNullable(get()).map(Progress::progress).orElse(Double.NaN);
    }

    public void setProgress(double progress) {
        Progress newProgress = Optional.ofNullable(get())
            .map(p -> new Progress(p.description(), progress))
            .orElseGet(() -> new Progress("", progress));
        set(newProgress);
    }

    public void set(String description, double progress) {
        set(new Progress(description, progress));
    }
}
