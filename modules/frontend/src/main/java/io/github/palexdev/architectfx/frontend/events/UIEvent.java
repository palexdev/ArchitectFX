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

package io.github.palexdev.architectfx.frontend.events;

import io.github.palexdev.architectfx.frontend.views.base.View;
import io.github.palexdev.mfxcore.events.Event;

public abstract class UIEvent extends Event {

    //================================================================================
    // Constructors
    //================================================================================
    public UIEvent() {}

    public UIEvent(Object data) {
        super(data);
    }

    //================================================================================
    // Impl
    //================================================================================

    @SuppressWarnings("unchecked")
    public static class ViewSwitchEvent extends UIEvent {
        public ViewSwitchEvent(Class<? extends View<?>> view) {
            super(view);
        }

        @Override
        public Class<? extends View<?>> data() {
            return (Class<? extends View<?>>) super.data();
        }
    }
}
