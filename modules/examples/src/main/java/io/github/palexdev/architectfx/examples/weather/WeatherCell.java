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

package io.github.palexdev.architectfx.examples.weather;

import io.github.palexdev.architectfx.examples.weather.WeatherAPI.WeatherConditions;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.virtualizedfx.cells.CellBaseBehavior;
import io.github.palexdev.virtualizedfx.cells.VFXCellBase;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class WeatherCell extends VFXCellBase<WeatherConditions> {

    //================================================================================
    // Constructors
    //================================================================================
    public WeatherCell(WeatherConditions item) {
        super(item);
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    protected SkinBase<?, ?> buildSkin() {
        return new SkinBase<>(this) {
            {
                Label dateLabel = new Label();
                dateLabel.getStyleClass().add("date");
                dateLabel.textProperty().bind(itemProperty().map(c ->
                    "Date: " + c.date()
                ));

                Label tempLabel = new Label();
                tempLabel.getStyleClass().add("temp");
                tempLabel.textProperty().bind(itemProperty().map(d ->
                    WeatherAppModel.useMetric() ? d.tempC() + "°C" : d.tempF() + "°F"
                ));

                getChildren().setAll(new VBox(dateLabel, tempLabel));
            }

            @Override
            protected void initBehavior(CellBaseBehavior<WeatherConditions> behavior) {
            }
        };
    }

    @Override
    public List<String> defaultStyleClasses() {
        return List.of("cell");
    }
}
