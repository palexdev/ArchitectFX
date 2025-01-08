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

package io.github.palexdev.architectfx.frontend.events;

import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.mfxcore.events.Event;

public abstract class ModelEvent extends Event {

    //================================================================================
    // Constructors
    //================================================================================
    public ModelEvent() {}

    public ModelEvent(Object data) {
        super(data);
    }

    //================================================================================
    // Impl
    //================================================================================
    public static class ProjectDeletedEvent extends ModelEvent {

        public ProjectDeletedEvent(Project project) {
            super(project);
        }

        @Override
        public Project data() {
            return (Project) super.data();
        }
    }
}
