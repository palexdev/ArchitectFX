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

import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import io.github.palexdev.architectfx.examples.Launcher;
import io.github.palexdev.mfxcomponents.theming.base.Theme;
import java.io.IOException;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

public class RootPane extends BorderPane {
    //================================================================================
    // Properties
    //================================================================================
    public static String TITLE = "App Title";
    public static String ICON_DESCRIPTION = "";
    public static Theme APP_THEME;
    private final Stage stage;
    private final ThemeEngine themeEngine = new ThemeEngine();

    //================================================================================
    // Constructors
    //================================================================================
    public RootPane(Stage stage) {
        this.stage = stage;
        setMinSize(800, 600);

        setTop(loadHeader());
        setBottom(loadFooter());
        getStyleClass().add("root-pane");
    }

    //================================================================================
    // Methods
    //================================================================================
    protected Node loadHeader() {
        try {
            JUIFXLoader loader = new JUIFXLoader();
            loader.config().setResolverFactory(uri -> {
                Resolver.Context ctx = new Resolver.Context(uri);
                ctx.setInjections(
                    "mainWindow", stage,
                    "rootPane", this
                );
                return new DefaultResolver(ctx);
            });
            return loader.load(Launcher.load("common/WindowHeader.jui")).root();
        } catch (IOException ex) {
            Logger.error("Failed to load header:\n{}", ex);
        }
        return null;
    }

    protected Node loadFooter() {
        try {
            JUIFXLoader loader = new JUIFXLoader();
            loader.config().setResolverFactory(uri -> {
                Resolver.Context ctx = new Resolver.Context(uri);
                ctx.setInjections("themeEngine", themeEngine);
                return new DefaultResolver(ctx);
            });
            return loader.load(Launcher.load("common/WindowFooter.jui")).root();
        } catch (IOException ex) {
            Logger.error("Failed to load footer:\n{}", ex);
        }
        return null;
    }
}
