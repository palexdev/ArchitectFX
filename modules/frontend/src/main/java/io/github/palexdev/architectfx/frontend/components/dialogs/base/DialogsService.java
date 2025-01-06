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

import io.github.palexdev.architectfx.frontend.components.dialogs.DialogType;
import io.github.palexdev.architectfx.frontend.components.dialogs.ProgressDialog;
import io.github.palexdev.architectfx.frontend.events.DialogEvent;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import javafx.stage.Window;

public interface DialogsService {

    default <D extends Dialog> void showDialog(DialogType type, DialogConfigurator<D> config) {
        type.show(this, config);
    }

    void showProgressDialog(DialogConfigurator<Dialog> config);

    //================================================================================
    // Impl
    //================================================================================

    @SuppressWarnings("unchecked")
    @Bean
    class DialogsServiceImpl implements DialogsService {

        public DialogsServiceImpl(IEventBus events, Window mainWindow) {
            DialogConfigurator.DialogConfig.mainWindow = mainWindow;
            events.subscribe(DialogEvent.ShowDialog.class, e -> showDialog(e.getType(), e.getConfigurator()));
        }

        @Override
        public void showProgressDialog(DialogConfigurator<Dialog> config) {
            ProgressDialog dialog = new ProgressDialog();
            DialogConfigurator.DialogConfig<Dialog> configuration = config.get();
            configuration.configureAndShow(dialog);
        }
    }
}
