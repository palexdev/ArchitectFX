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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.github.palexdev.architectfx.frontend.Resources;
import io.github.palexdev.mfxcomponents.theming.base.Theme;
import org.tinylog.Logger;

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
        // FIXME this is to speed things up, but ideally the ZIP should be pre-built
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(boas)) {
            String[] assets = new String[]{
                "css/Overrides.css",
                "css/components/Components.css",
                "css/components/DnDComponent.css",
                "css/fonts/Fonts.css",
                "css/fonts/montserrat/Montserrat.css",
                "css/fonts/montserrat/Montserrat-Black.ttf",
                "css/fonts/montserrat/Montserrat-BlackItalic.ttf",
                "css/fonts/montserrat/Montserrat-Bold.ttf",
                "css/fonts/montserrat/Montserrat-BoldItalic.ttf",
                "css/fonts/montserrat/Montserrat-ExtraBold.ttf",
                "css/fonts/montserrat/Montserrat-ExtraBoldItalic.ttf",
                "css/fonts/montserrat/Montserrat-ExtraLight.ttf",
                "css/fonts/montserrat/Montserrat-ExtraLightItalic.ttf",
                "css/fonts/montserrat/Montserrat-Italic.ttf",
                "css/fonts/montserrat/Montserrat-Light.ttf",
                "css/fonts/montserrat/Montserrat-LightItalic.ttf",
                "css/fonts/montserrat/Montserrat-Medium.ttf",
                "css/fonts/montserrat/Montserrat-MediumItalic.ttf",
                "css/fonts/montserrat/Montserrat-Regular.ttf",
                "css/fonts/montserrat/Montserrat-SemiBold.ttf",
                "css/fonts/montserrat/Montserrat-SemiBoldItalic.ttf",
                "css/fonts/montserrat/Montserrat-Thin.ttf",
                "css/fonts/montserrat/Montserrat-ThinItalic.ttf",
                "css/views/Views.css",
                "css/views/InitView.css"
            };

            zos.putNextEntry(new ZipEntry("components/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("fonts/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("fonts/montserrat/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("views/"));
            zos.closeEntry();

            for (String asset : assets) {
                try (InputStream is = Resources.loadStream(asset)) {
                    if (is != null) {
                        zos.putNextEntry(new ZipEntry(asset.replace("css/", "")));
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                        zos.closeEntry();
                    } else {
                        Logger.error("Could not load asset: {}", asset);
                    }
                } catch (NullPointerException ex) {
                    Logger.error("Could not load asset: {}\n{}", asset, ex.getMessage());
                }
            }
        } catch (IOException ex) {
            Logger.error(ex.getMessage());
            return null;
        }

        // Convert to ByteArrayInputStream after the ZipOutputStream is fully closed
        return new ByteArrayInputStream(boas.toByteArray());
    }

    @Override
    public String deployName() {
        return "architectfx-assets";
    }
}
