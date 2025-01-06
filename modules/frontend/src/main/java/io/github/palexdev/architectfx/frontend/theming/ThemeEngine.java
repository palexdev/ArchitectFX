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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

@Bean
public class ThemeEngine {
    //================================================================================
    // Properties
    //================================================================================
    private final IEventBus events;
    private MaterialThemes currentMaterialTheme; // TODO generalize in the future
    private final ObjectProperty<ThemeMode> themeMode = new SimpleObjectProperty<>() {
        @Override
        public void set(ThemeMode newValue) {
            ThemeMode oldValue = get();
            super.set(newValue);
            if (oldValue != null) {
                ThemeEngine.this.currentMaterialTheme = currentMaterialTheme.getVariant();
                events.publish(new UIEvent.ThemeSwitchEvent());
                loadTheme();
            }
        }
    };

    //================================================================================
    // Constructors
    //================================================================================
    public ThemeEngine(IEventBus events, AppSettings settings) {
        this.events = events;
        try {
            setThemeMode(EnumUtils.valueOfIgnoreCase(
                ThemeMode.class,
                settings.themeMode().get()
            ));
        } catch (Exception ex) {
            Logger.warn("Warning, corrupted theme settings! Fallback to default");
            setThemeMode(ThemeMode.valueOf(settings.themeMode().defValue()));
        }
        events.subscribe(AppEvent.AppCloseEvent.class,e -> settings.themeMode().set(getThemeMode().name()));
    }

    //================================================================================
    // Methods
    //================================================================================
    public void loadTheme() {
        if (currentMaterialTheme == null)
            currentMaterialTheme = getThemeMode() == ThemeMode.LIGHT ? MaterialThemes.PURPLE_LIGHT : MaterialThemes.PURPLE_DARK;
        UserAgentBuilder.builder()
            .themes(currentMaterialTheme)
            .themes(AppTheme.DEFAULT)
            .setDeploy(true)
            .setResolveAssets(true)
            .build()
            .setGlobal();
    }

    public ThemeMode getThemeMode() {
        return themeMode.get();
    }

    public ObjectProperty<ThemeMode> themeModeProperty() {
        return themeMode;
    }

    @BeanSocket(enabled = false)
    public void setThemeMode(ThemeMode mode) {
        this.themeMode.set(mode);
    }

    public void nextMode() {
        ThemeMode next = EnumUtils.next(ThemeMode.class, getThemeMode());
        setThemeMode(next);
    }
}
