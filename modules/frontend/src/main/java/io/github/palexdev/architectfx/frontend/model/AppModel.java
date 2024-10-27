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

package io.github.palexdev.architectfx.frontend.model;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.github.palexdev.architectfx.frontend.enums.Tool;
import io.github.palexdev.architectfx.frontend.events.AppEvent.AppCloseEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.architectfx.frontend.utils.DateTimeUtils;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.inverno.core.annotation.Bean;
import javafx.collections.ObservableList;

@Bean
public class AppModel {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableList<Recent> recents;
    private final AppSettings settings;
    private final IEventBus events;

    //================================================================================
    // Constructors
    //================================================================================
    public AppModel(AppSettings settings, IEventBus events) {
        this.settings = settings;
        this.events = events;
        this.recents = settings.loadRecents();
        events.subscribe(AppCloseEvent.class, e -> settings.saveRecents(recents));
    }

    //================================================================================
    // Methods
    //================================================================================
    public void run(Tool tool, File file) {
        try {
            tool.load(file);

            // Save tool for next session
            settings.lastTool().set(tool.name());

            // We have to work with lists for simplicity, but we need to make sure that there are no duplicate files!
            Recent recent = new Recent(file.toPath(), DateTimeUtils.epochMilli());
            Set<Recent> tmp = new HashSet<>(recents);
            if (tmp.add(recent)) {
                recents.add(recent);
            }

            settings.lastDir().set(file.getParent());
        } catch (Exception ex) {

        }
    }

    //================================================================================
    // Getters
    //================================================================================
    public ObservableList<Recent> recents() {
        return recents;
    }
}
