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

package io.github.palexdev.architectfx.examples.notes;

import io.github.palexdev.architectfx.backend.loaders.jui.JUIFXLoader;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import io.github.palexdev.architectfx.examples.Launcher;
import io.github.palexdev.architectfx.examples.common.RootPane;
import io.github.palexdev.mfxcomponents.theming.base.Theme;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.tinylog.Logger;

public class NotesApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        RootPane.TITLE = "Notes";
        RootPane.ICON_DESCRIPTION = "fas-note-sticky";
        RootPane.APP_THEME = new Theme() {
            @Override
            public String name() {
                return "NotesTheme";
            }

            @Override
            public String path() {
                return "notes/Theme.css";
            }

            @Override
            public URL asURL(String path) {
                return Launcher.load(path);
            }
        };

        RootPane root = new RootPane(stage);
        root.setCenter(loadContent(stage));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

    private Node loadContent(Stage stage) {
        try {
            JUIFXLoader loader = new JUIFXLoader();
            loader.config().setResolverFactory(uri -> {
                Resolver.Context ctx = new Resolver.Context(uri);
                ctx.setInjections(
                    "mainWindow", stage,
                    "model", new NotesAppModel()
                );
                return new DefaultResolver(ctx);
            });
            return loader.load(Launcher.load("notes/Notes.jui")).root();
        } catch (IOException ex) {
            Logger.error("Failed to load UI:\n{}", ex);
        }
        return null;
    }
}
