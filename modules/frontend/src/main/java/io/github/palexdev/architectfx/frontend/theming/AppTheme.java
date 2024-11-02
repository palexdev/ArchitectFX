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

import java.io.InputStream;
import java.net.URL;

import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.mfxcomponents.theming.base.Theme;

public enum AppTheme implements Theme {
    DEFAULT("css/AppTheme.css"),
    ;

    private final String path;

    AppTheme(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public URL asURL(String path) {
        return Resources.loadURL(path);
    }

    @Override
    public InputStream assets() {
        return Resources.loadStream("assets/assets.zip");
    }

    @Override
    public String deployName() {
        return "architectfx-assets";
    }
}
