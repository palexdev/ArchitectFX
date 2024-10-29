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

package io.github.palexdev.architectfx.frontend.components.dialogs.base;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.frontend.ArchitectFX;
import io.github.palexdev.architectfx.frontend.components.dialogs.ProgressDialog;
import io.github.palexdev.architectfx.frontend.events.DialogEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Window;

public interface DialogsService {

    void showProgressDialog(Supplier<DialogConfig<ProgressDialog>> configFn);

    //================================================================================
    // Impl
    //================================================================================
    @Bean
    class DialogsServiceImpl implements DialogsService {

        public DialogsServiceImpl(IEventBus events) {
            events.subscribe(DialogEvent.ShowProgress.class, e -> showProgressDialog(e.data()));
        }

        @Override
        public void showProgressDialog(Supplier<DialogConfig<ProgressDialog>> configFn) {
            ProgressDialog dialog = new ProgressDialog();
            DialogConfig<ProgressDialog> config = configFn.get();
            config.show(dialog);
        }
    }

    //================================================================================
    // Config
    //================================================================================
    class DialogConfig<D extends Dialog> {
        private static Window mainWindow;
        private Object owner;
        private Pos pos = Pos.CENTER;
        private Consumer<D> configurator = d -> {};

        public void show(D dialog) {
            configurator.accept(dialog);
            switch (owner) {
                case Window w -> dialog.show(w, pos);
                case Node n -> dialog.show(n, pos);
                case null, default -> throw new IllegalStateException("Dialog's owner is null!");
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
                if (mainWindow == null) return this;
            }
            owner = mainWindow;
            return this;
        }

        public DialogConfig<D> extraConfig(Consumer<D> extraConfig) {
            configurator = configurator.andThen(extraConfig);
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
            this.configurator = configurator.andThen(d -> d.setScrimOwner(scrimOwner));
            return this;
        }

        public DialogConfig<D> setScrimStrength(double scrimStrength) {
            this.configurator = configurator.andThen(d -> d.setScrimStrength(scrimStrength));
            return this;
        }
    }
}
