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

import io.github.palexdev.architectfx.frontend.model.Recent;
import io.github.palexdev.architectfx.frontend.theming.ThemeMode;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
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
    private final NumberSetting<Double> windowWidth = registerDouble("window.width", "", 1200.0);
    private final NumberSetting<Double> windowHeight = registerDouble("window.height", "", 500.0);
    private final StringSetting themeMode = registerString("theme.mode", "Theme variation, light/dark", ThemeMode.LIGHT.name());

    // App
    private final StringSetting recents = registerString("recents", "YAML string that contains recently opened documents", "");
    private final StringSetting lastDir = registerString("last.dir", "Last directory used for file input", "");
    private final StringSetting lastTool = registerString("last.tool", "Last tool used for file input", "PREVIEW");

    // Live Preview
    private final BooleanSetting autoReload = registerBoolean("autoreload", "Specifies whether to automatically reload the document in case of modifications", true);
    private final NumberSetting<Integer> autoReloadDelay = registerInteger("autoreload.delay", "Delay seconds for LivePreview auto-reload", 5);

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
    public List<Recent> loadRecents() {
        String yaml = recents.get();
        return Recent.load(yaml);
    }

    public void saveRecents(Collection<Recent> recents) {
        String toYaml = Recent.save(recents);
        this.recents.set(toYaml);
    }

    public Size getWindowSize() {
        return UIUtils.getWindowSize(
            windowWidth.defValue(), windowHeight.defValue(),
            windowWidth.get(), windowHeight.get()
        );
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

    public StringSetting lastTool() {
        return lastTool;
    }

    public BooleanSetting autoReload() {
        return autoReload;
    }

    public NumberSetting<Integer> autoReloadDelay() {
        return autoReloadDelay;
    }

    public boolean isResetSettings() {
        if (resetSettings == null) {
            Map<String, String> named = parameters.getNamed();
            resetSettings = Boolean.parseBoolean(named.getOrDefault("reset-settings", "false"));
        }
        return resetSettings;
    }
}
