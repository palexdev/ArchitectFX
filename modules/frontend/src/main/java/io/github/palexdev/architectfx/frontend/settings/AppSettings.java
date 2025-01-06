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

package io.github.palexdev.architectfx.frontend.settings;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.palexdev.architectfx.frontend.model.Project;
import io.github.palexdev.architectfx.frontend.theming.ThemeMode;
import io.github.palexdev.mfxcore.base.beans.Size;
import io.github.palexdev.mfxcore.settings.BooleanSetting;
import io.github.palexdev.mfxcore.settings.NumberSetting;
import io.github.palexdev.mfxcore.settings.Settings;
import io.github.palexdev.mfxcore.settings.StringSetting;
import io.inverno.core.annotation.Bean;
import javafx.application.Application;

@Bean
public class AppSettings extends Settings {
    //================================================================================
    // Settings
    //================================================================================
    // UI
    private final NumberSetting<Double> windowWidth = registerDouble("window.width", "", 1280.0);
    private final NumberSetting<Double> windowHeight = registerDouble("window.height", "", 720.0);
    private final StringSetting themeMode = registerString("theme.mode", "Theme variation, light/dark", ThemeMode.LIGHT.name());

    // Model
    private final StringSetting lastDir = registerString("last.dir", "", "");
    private final StringSetting projects = registerString("projects", "Array specifying the projects known to the app", "[]");
    private final StringSetting projectsSort = registerString("projects.sort", "Last sort type for projects", Project.SortBy.NAME.name());
    private final StringSetting projectsSortMode = registerString("projects.sortmode", "Last sort mode for projects", Project.SortMode.ASCENDING.name());
    private final BooleanSetting autoReload = registerBoolean("reload.autoreload", "Whether to auto-reload a project after its file changed", true);
    private final NumberSetting<Integer> reloadCountdown = registerInteger("reload.countdown", "Seconds after which to reload a project for which the file has changed", 3);

    // Extra
    private final Application.Parameters parameters;
    private Boolean resetSettings = null;

    //================================================================================
    // Constructors
    //================================================================================
    public AppSettings(Application.Parameters parameters) {
        this.parameters = parameters;
    }

    //================================================================================
    // Methods
    //================================================================================
    public Size getWindowSize() {
        return Size.of(windowWidth.get(), windowHeight.get());
    }

    public List<Project> loadProjects() {
        String s = projects.get();
        return Project.fromString(s);
    }

    public void saveProjects(Collection<Project> projects) {
        String s = Project.toString(projects);
        this.projects.set(s);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected String node() {
        return "/io/github/palexdev/architectfx";
    }

    //================================================================================
    // Getters
    //================================================================================
    public NumberSetting<Double> windowWidth() {
        return windowWidth;
    }

    public NumberSetting<Double> windowHeight() {
        return windowHeight;
    }

    public StringSetting themeMode() {
        return themeMode;
    }

    public StringSetting lastDir() {
        return lastDir;
    }

    public StringSetting getProjectsSort() {
        return projectsSort;
    }

    public StringSetting getProjectsSortMode() {
        return projectsSortMode;
    }

    public BooleanSetting getAutoReload() {
        return autoReload;
    }

    public NumberSetting<Integer> getReloadCountdown() {
        return reloadCountdown;
    }

    public boolean isResetSettings() {
        if (resetSettings == null) {
            Map<String, String> named = parameters.getNamed();
            resetSettings = Boolean.parseBoolean(named.getOrDefault("reset-settings", "false"));
        }
        return resetSettings;
    }
}
