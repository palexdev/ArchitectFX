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

package io.github.palexdev.architectfx.frontend.views;

import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public abstract class View<P extends Pane> {
    //================================================================================
    // Properties
    //================================================================================
    protected P root;
    protected final IEventBus events;

    //================================================================================
    // Constructors
    //================================================================================
    protected View(IEventBus events) {
        this.events = events;
        events.subscribe(AppEvent.AppReadyEvent.class, e -> onAppReady());
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract P build();

    //================================================================================
    // Methods
    //================================================================================
    public Region toRegion() {
        if (root == null) {
            root = build();
        }
        return root;
    }

    public String title() {
        return "";
    }

    protected void onAppReady() {}

    protected void onSwitching() {}
}
