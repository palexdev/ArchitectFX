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

import java.util.function.BiFunction;

import io.github.palexdev.architectfx.frontend.views.View;
import io.github.palexdev.mfxcore.events.Event;
import javafx.animation.Animation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

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

    public static class ViewSwitchEvent extends UIEvent {
        private final Pane parent;
        private final Class<? extends View<?, ?>> view;
        private final BiFunction<Node, Node, Animation> animation;

        public ViewSwitchEvent(Class<? extends View<?, ?>> view) {
            this(null, view, null);
        }

        public ViewSwitchEvent(Pane parent, Class<? extends View<?, ?>> view) {
            this(parent, view, null);
        }

        public ViewSwitchEvent(Pane parent, Class<? extends View<?, ?>> view, BiFunction<Node, Node, Animation> animation) {
            super(new Object[] {parent, view, animation});
            this.parent = parent;
            this.view = view;
            this.animation = animation;
        }

        public Pane parent() {
            return parent;
        }

        public Class<? extends View<?, ?>> view() {
            return view;
        }

        public BiFunction<Node, Node, Animation> animation() {
            return animation;
        }

        @Override
        public Object[] data() {
            return (Object[]) super.data();
        }
    }

    public static class ThemeSwitchEvent extends UIEvent {}
}