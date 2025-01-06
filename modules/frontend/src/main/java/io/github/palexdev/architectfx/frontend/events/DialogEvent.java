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

import io.github.palexdev.architectfx.frontend.components.dialogs.DialogType;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.Dialog;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.DialogConfigurator;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.DialogConfigurator.DialogConfig;
import io.github.palexdev.mfxcore.events.Event;

public abstract class DialogEvent extends Event {

    //================================================================================
    // Constructors
    //================================================================================
    public DialogEvent() {}

    public DialogEvent(Object data) {
        super(data);
    }

    //================================================================================
    // Impl
    //================================================================================
    public static class ShowDialog<D extends Dialog> extends DialogEvent {
        private final DialogType type;
        private final DialogConfigurator<D> configurator;

        public ShowDialog(DialogType type) {
            this(type, DialogConfig::new);
        }

        public ShowDialog(DialogType type, DialogConfigurator<D> configurator) {
            super(new Object[]{type, configurator});
            this.type = type;
            this.configurator = configurator;
        }

        public DialogType getType() {
            return type;
        }

        public DialogConfigurator<D> getConfigurator() {
            return configurator;
        }

        @Override
        public Object[] data() {
            return (Object[]) super.data();
        }
    }
}
