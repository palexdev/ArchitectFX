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

import io.github.palexdev.architectfx.backend.model.Initializable;
import io.github.palexdev.architectfx.examples.common.TextField;
import io.github.palexdev.architectfx.examples.weather.WeatherAPI.WeatherConditions;
import io.github.palexdev.architectfx.examples.weather.WeatherAPI.WeatherData;
import io.github.palexdev.mfxcore.builders.bindings.DoubleBindingBuilder;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.virtualizedfx.list.VFXList;
import io.github.palexdev.virtualizedfx.list.VFXListHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class WeatherController implements Initializable {
    private WeatherAppModel appModel;

    private TextField locField;

    private Label locLabel;
    private Label condLabel;
    private Label tempLabel;
    private Label windLabel;

    private VFXList<WeatherConditions, WeatherCell> forecastList;

    @Override
    public void initialize() {
        When.onInvalidated(appModel.dataProperty())
            .then(this::update)
            .listen();

        locField.getTrailingGraphic().addEventHandler(ActionEvent.ACTION, e -> {
                String loc = locField.getText();
                appModel.fetch(loc);
            }
        );

        forecastList.setCellFactory(WeatherCell::new);
        forecastList.setOrientation(Orientation.HORIZONTAL);

        appModel.fetch("");
    }

    private void update(WeatherData data) {
        locLabel.setText("%s (%s, %s)".formatted(data.area(), data.country(), data.region()));
        if (data.forecasts().isEmpty()) return;
        WeatherConditions curr = data.forecasts().getFirst();
        condLabel.setText(curr.description());
        tempLabel.setText(WeatherAppModel.useMetric() ? curr.tempC() + "°C" : curr.tempF() + "°F");
        windLabel.setText(WeatherAppModel.useMetric() ? curr.windK() + " Km/s" : curr.windM() + " mph");

        forecastList.maxWidthProperty().bind(DoubleBindingBuilder.build()
            .setMapper(() -> {
                double spacing = forecastList.size() * forecastList.getSpacing();
                double cellS = forecastList.size() * forecastList.getCellSize();
                return spacing + cellS;
            })
            .addSources(forecastList.itemsProperty(), forecastList.spacingProperty(), forecastList.cellSizeProperty())
            .get()
        );
        forecastList.setMaxHeight(100.0);
        forecastList.setHelperFactory(o -> new VFXListHelper.HorizontalHelper<>(forecastList) {
            @Override
            public double computeSize(Node node) {
                /* FIXME on VirtualizedFX side */
                return forecastList.getHeight() - forecastList.snappedBottomInset() - forecastList.snappedTopInset();
            }
        });
        forecastList.setItems(FXCollections.observableArrayList(
            data.forecasts().subList(1, data.forecasts().size()))
        );
    }
}
