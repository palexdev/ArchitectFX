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

package io.github.palexdev.architectfx.frontend.components.dialogs.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.components.dialogs.base.DialogConfigurator.DialogConfig;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Window;

@FunctionalInterface
public interface DialogConfigurator<D extends Dialog> extends Supplier<DialogConfig<D>> {

    //================================================================================
    // Inner Classes
    //================================================================================
    class DialogConfig<D extends Dialog> {
        static Window mainWindow;
        private Object owner;
        private Pos pos = Pos.CENTER;
        private Consumer<D> extraConfig;

        public void configureAndShow(D dialog) {
            if (extraConfig != null) extraConfig.accept(dialog);
            switch (owner) {
                case Window w -> dialog.show(w, pos);
                case Node n -> dialog.show(n, pos);
                case null, default -> throw new IllegalStateException("Invalid dialog's owner " + owner);
            }
        }

        public DialogConfig<D> implicitOwner() {
            if (mainWindow == null) {
                mainWindow = Window.getWindows().stream()
                    .filter(Stage.class::isInstance)
                    .map(Stage.class::cast)
                    .filter(s -> s.getTitle().startsWith(ArchitectFX.APP_TITLE))
                    .findFirst()
                    .orElse(null);
            }
            owner = mainWindow;
            return this;
        }

        public DialogConfig<D> extraConfig(Consumer<D> extraConfig) {
            if (this.extraConfig == null) {
                this.extraConfig = extraConfig;
                return this;
            }
            this.extraConfig = this.extraConfig.andThen(extraConfig);
            return this;
        }

        public DialogConfig<D> setOwner(Window owner) {
            this.owner = owner;
            return this;
        }

        public DialogConfig<D> setOwner(Node owner) {
            this.owner = owner;
            return this;
        }

        public DialogConfig<D> setPos(Pos pos) {
            this.pos = pos;
            return this;
        }

        public DialogConfig<D> setScrimOwner(boolean scrimOwner) {
            return extraConfig(d -> d.setScrimOwner(scrimOwner));
        }

        public DialogConfig<D> setScrimStrength(double scrimStrength) {
            return extraConfig(d -> d.setScrimStrength(scrimStrength));
        }
    }
}