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

package io.github.palexdev.architectfx.frontend.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;

import io.github.palexdev.architectfx.backend.utils.CastUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

public record Recent(Path file, long timestamp) {
    public Recent {
        if (!isValid(file, timestamp))
            throw new IllegalArgumentException(
                "Invalid recent:\n- %s\n- %d"
                    .formatted(file, timestamp)
            );
    }

    public static boolean isValid(Path file, long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return file != null &&
               Files.exists(file) &&
               !Files.isDirectory(file) &&
               (instant.isBefore(Instant.MAX) && instant.isAfter(Instant.MIN));
    }

    public static ObservableList<Recent> load(String yaml) {
        ObservableList<Recent> recents = FXCollections.observableArrayList();
        try {
            SequencedMap<String, Object> map = new Yaml().load(yaml);
            List<Object> list = CastUtils.asGenericList(map.get("recents"));
            for (Object obj : list) {
                SequencedMap<String, Object> rMap = CastUtils.asYamlMap(obj);
                Path file = Path.of((String) rMap.get("file"));
                long timestamp = (long) rMap.get("timestamp");
                try {
                    Recent recent = new Recent(file, timestamp);
                    recents.add(recent);
                } catch (IllegalArgumentException ex) {
                    Logger.warn(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Logger.error("Failed to load recents:\n{}", ex);
        }
        return recents;
    }

    public static String save(Collection<Recent> recents) {
        if (recents.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("recents: \n");
        for (Recent recent : recents) {
            if (!Recent.isValid(recent.file, recent.timestamp)) {
                Logger.warn("Invalid recent:\n- {}\n- {}", recent.file, recent.timestamp);
                continue;
            }
            sb.append("  ");
            sb.append("- {");
            sb.append("file: \"").append(recent.file.toString().replace("\\", "\\\\")).append("\"");
            sb.append(", ");
            sb.append("timestamp: ").append(recent.timestamp);
            sb.append("}");
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recent recent = (Recent) o;
        return Objects.equals(file, recent.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }
}