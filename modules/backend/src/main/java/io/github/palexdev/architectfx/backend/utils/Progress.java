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

/// Simple record to express the progress of a certain task. Allows you to specify a description of the progress
/// along of course the progress value, expressed in values between `-1.0` and `1.0`.
public record Progress(String description, double progress) {
    //================================================================================
    // Static Properties
    //================================================================================

    /// Special progress with value `-1.0` that signals an indeterminate state.
    public static final Progress INDETERMINATE = new Progress("Indeterminate", -1.0);

    /// Special progress that can be used to indicate that a certain task has been canceled.
    public static final Progress CANCELED = new Progress("Canceled", Double.NaN);

    //================================================================================
    // Constructors
    //================================================================================
    public Progress {
        progress = Math.clamp(progress, -1.0, 1.0);
    }

    public static Progress of(String description, double progress) {
        return new Progress(description, progress);
    }
}
