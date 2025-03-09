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

package io.github.palexdev.architectfx.examples.common;

import io.github.palexdev.architectfx.examples.Launcher;
import io.github.palexdev.mfxcomponents.theming.MaterialThemes;
import io.github.palexdev.mfxcomponents.theming.UserAgentBuilder;
import io.github.palexdev.mfxcomponents.theming.base.Theme;
import java.io.InputStream;
import java.net.URL;

public class ThemeEngine {
    //================================================================================
    // Properties
    //================================================================================
    private final Theme LIGHT_THEME = MaterialThemes.INDIGO_LIGHT;
    private final Theme DARK_THEME = MaterialThemes.INDIGO_DARK;
    private final Theme COMMON_THEME = new Theme() {
        @Override
        public String name() {
            return "CommonTheme";
        }

        @Override
        public String path() {
            return "common/CommonTheme.css";
        }

        @Override
        public URL asURL(String path) {
            return Launcher.load(path);
        }

        @Override
        public InputStream assets() {
            return Launcher.loadStream("assets.zip");
        }
    };
    private ThemeMode mode = ThemeMode.LIGHT;

    //================================================================================
    // Constructors
    //================================================================================
    public ThemeEngine() {
        loadTheme();
    }

    //================================================================================
    // Methods
    //================================================================================
    public void setThemeMode(ThemeMode mode) {
        this.mode = mode;
        loadTheme();
    }

    protected void loadTheme() {
        UserAgentBuilder.builder()
            .themes(mode == ThemeMode.LIGHT ? LIGHT_THEME : DARK_THEME)
            .themes(COMMON_THEME)
            .themes(RootPane.APP_THEME)
            .setDeploy(true)
            .setResolveAssets(true)
            .build()
            .setGlobal();
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public enum ThemeMode {
        LIGHT, DARK
    }
}
