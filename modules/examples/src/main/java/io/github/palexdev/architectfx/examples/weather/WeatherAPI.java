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

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.tinylog.Logger;

public class WeatherAPI {
    //================================================================================
    // Properties
    //================================================================================
    private final String ENDPOINT = "https://wttr.in/%s?m&format=j1";

    //================================================================================
    // Methods
    //================================================================================
    public WeatherData fetch(String location) {
        try {
            location = location.replace(" ", "+");
            URL url = URI.create(ENDPOINT.formatted(location)).toURL();
            URLConnection connection = url.openConnection();
            ((HttpsURLConnection) connection).setRequestMethod("GET");
            String response = read(connection);
            if (response.isBlank()) return WeatherData.EMPTY;

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            WeatherData data = parseData(json);
            for (WeatherConditions c : parseForecasts(json)) data.addForecast(c);
            return data;
        } catch (Exception ex) {
            Logger.error("Failed to fetch weather data because:\n{}", ex);
            return new WeatherData("Error", "", "");
        }
    }

    private WeatherData parseData(JsonObject json) {
        JsonObject areaObj = json.getAsJsonArray("nearest_area").get(0).getAsJsonObject();
        String areaName = areaObj.getAsJsonArray("areaName").get(0)
            .getAsJsonObject()
            .get("value")
            .getAsString();
        String country = areaObj.getAsJsonArray("country").get(0)
            .getAsJsonObject()
            .get("value")
            .getAsString();
        String region = areaObj.getAsJsonArray("region").get(0)
            .getAsJsonObject()
            .get("value")
            .getAsString();
        return new WeatherData(areaName, country, region);
    }

    private List<WeatherConditions> parseForecasts(JsonObject json) {
        List<WeatherConditions> list = new ArrayList<>();
        list.add(parseCurrent(json));
        for (JsonElement e : json.getAsJsonArray("weather")) {
            list.add(parseForecast(e.getAsJsonObject()));
        }
        return list;
    }

    private WeatherConditions parseCurrent(JsonObject json) {
        JsonObject currJson = json.getAsJsonArray("current_condition").get(0).getAsJsonObject();
        String date = currJson.get("localObsDateTime").getAsString();
        String desc = currJson.getAsJsonArray("weatherDesc").get(0)
            .getAsJsonObject()
            .get("value")
            .getAsString();
        String tempC = currJson.get("temp_C").getAsString();
        String tempF = currJson.get("temp_F").getAsString();
        String windK = currJson.get("windspeedKmph").getAsString();
        String windM = currJson.get("windspeedMiles").getAsString();
        return new WeatherConditions(date, desc, tempC, tempF, windK, windM);
    }

    private WeatherConditions parseForecast(JsonObject json) {
        String date = json.get("date").getAsString();
        String tempC = json.get("avgtempC").getAsString();
        String tempF = json.get("avgtempF").getAsString();
        return new WeatherConditions(date, "", tempC, tempF, "", "");
    }

    private String read(URLConnection connection) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream())
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public record WeatherData(
        String area,
        String country,
        String region,
        List<WeatherConditions> forecasts
    ) {
        public static final WeatherData EMPTY = new WeatherData("Unavailable", "", "");

        public WeatherData(String area, String country, String region) {
            this(area, country, region, new ArrayList<>());
        }

        public void addForecast(WeatherConditions forecast) {
            this.forecasts.add(forecast);
        }
    }

    public record WeatherConditions(
        String date,
        String description,
        String tempC,
        String tempF,
        String windK,
        String windM
    ) {}
}
