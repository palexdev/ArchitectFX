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

package io.github.palexdev.architectfx.frontend.theming;

import io.github.palexdev.architectfx.frontend.events.AppEvent;
import io.github.palexdev.architectfx.frontend.events.UIEvent;
import io.github.palexdev.architectfx.frontend.settings.AppSettings;
import io.github.palexdev.mfxcomponents.theming.MaterialThemes;
import io.github.palexdev.mfxcomponents.theming.UserAgentBuilder;
import io.github.palexdev.mfxcore.events.bus.IEventBus;
import io.github.palexdev.mfxcore.utils.EnumUtils;
import io.inverno.core.annotation.Bean;
import io.inverno.core.annotation.BeanSocket;
import org.tinylog.Logger;

@Bean
public class ThemeEngine {
    //================================================================================
    // Properties
    //================================================================================
    private final IEventBus events;
    private MaterialThemes currentMaterialTheme;
    private ThemeMode mode;

    //================================================================================
    // Constructors
    //================================================================================
    public ThemeEngine(AppSettings settings, IEventBus events) {
        this.events = events;
        try {
            this.mode = EnumUtils.valueOfIgnoreCase(
                ThemeMode.class,
                settings.themeMode().get()
            );
        } catch (Exception ex) {
            Logger.warn("Warning, corrupted theme settings! Fallback to default");
            this.mode = ThemeMode.valueOf(settings.themeMode().defValue());
        }
        events.subscribe(AppEvent.AppCloseEvent.class, e -> settings.themeMode().set(mode.name()));
    }

    public void loadTheme() {
        if (currentMaterialTheme == null)
            currentMaterialTheme = mode == ThemeMode.LIGHT ? MaterialThemes.INDIGO_LIGHT : MaterialThemes.INDIGO_DARK;
        UserAgentBuilder.builder()
            .themes(currentMaterialTheme)
            .themes(AppTheme.DEFAULT)
            .setDeploy(true)
            .setResolveAssets(true)
            .build()
            .setGlobal();
    }

    public ThemeMode getThemeMode() {
        return mode;
    }

    @BeanSocket(enabled = false)
    public void setThemeMode(ThemeMode mode) {
        this.mode = mode;
        this.currentMaterialTheme = currentMaterialTheme.getVariant();
        events.publish(new UIEvent.ThemeSwitchEvent());
        loadTheme();
    }

    public void nextMode() {
        ThemeMode next = EnumUtils.next(ThemeMode.class, mode);
        setThemeMode(next);
    }
}
