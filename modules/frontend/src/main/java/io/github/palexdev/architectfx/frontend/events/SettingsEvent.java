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

import io.github.palexdev.mfxcore.events.Event;
import io.github.palexdev.mfxcore.settings.Settings;

public abstract class SettingsEvent extends Event {

    //================================================================================
    // Constructors
    //================================================================================
    public SettingsEvent() {}

    public SettingsEvent(Object data) {
        super(data);
    }

    //================================================================================
    // Impl
    //================================================================================
    public static class ResetSettingsEvent extends SettingsEvent {
        public ResetSettingsEvent(Class<? extends Settings> klass) {
            super(klass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Settings> data() {
            return ((Class<? extends Settings>) super.data());
        }
    }
}
