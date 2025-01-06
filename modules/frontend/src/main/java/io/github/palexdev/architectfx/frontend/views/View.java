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

import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public abstract class View<P extends Pane, B> {
    //================================================================================
    // Properties
    //================================================================================
    protected P root;
    protected B behavior;
    protected final IEventBus events;

    //================================================================================
    // Constructors
    //================================================================================
    protected View(IEventBus events) {
        this.events = events;
        this.behavior = behaviorSupplier().get();
        events.subscribe(AppEvent.AppReadyEvent.class, e -> onAppReady());
    }

    //================================================================================
    // Abstract Methods
    //================================================================================
    protected abstract P build();

    //================================================================================
    // Methods
    //================================================================================
    public String title() {
        return "";
    }

    public Region toRegion() {
        if (root == null) {
            root = build();
        }
        return root;
    }

    protected Supplier<B> behaviorSupplier() {
        return () -> null;
    }

    protected void onAppReady() {}
}
