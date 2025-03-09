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

import io.github.palexdev.architectfx.backend.utils.Async;
import io.github.palexdev.architectfx.examples.weather.WeatherAPI.WeatherData;
import java.util.Locale;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class WeatherAppModel {
    //================================================================================
    // Properties
    //================================================================================
    private final WeatherAPI api = new WeatherAPI();

    private final ObjectProperty<WeatherData> data = new SimpleObjectProperty<>();

    private Future<?> fetchTask;

    //================================================================================
    // Methods
    //================================================================================
    public void fetch(String location) {
        if (fetchTask != null) {
            fetchTask.cancel(true);
        }
        fetchTask = Async.call(() -> api.fetch(location))
            .thenAccept(d -> Platform.runLater(() -> setData(d)));
    }

    public static boolean useMetric() {
        String country = Locale.getDefault().getCountry();
        return !(country.equals("US") || country.equals("LR") || country.equals("MM"));
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public WeatherData getData() {
        return data.get();
    }

    public ReadOnlyObjectProperty<WeatherData> dataProperty() {
        return data;
    }

    protected void setData(WeatherData data) {
        this.data.set(data);
    }
}
