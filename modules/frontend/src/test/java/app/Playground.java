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

package app;

import io.github.palexdev.architectfx.frontend.components.CountdownIcon;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.mfxcomponents.theming.MaterialThemes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Playground extends Application {

    @Override
    public void start(Stage stage) {
        CountdownIcon indicator = new CountdownIcon(Duration.seconds(5));

        StackPane root = new StackPane(indicator);
        root.getStylesheets().add(MaterialThemes.INDIGO_LIGHT.toData());
        UIUtils.debugTheme(root, "css/views/LivePreview.css");
        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
        stage.setOnShown(e -> indicator.start());
        stage.show();
    }
}
