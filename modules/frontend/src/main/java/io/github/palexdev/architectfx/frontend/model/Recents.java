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
import java.util.*;

import io.github.palexdev.architectfx.backend.utils.CastUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

public class Recents {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableSet<Recent> observableSet;

    //================================================================================
    // Constructors
    //===============================================================================
    private Recents() {
        observableSet = FXCollections.observableSet(new TreeSet<>());
    }

    //================================================================================
    // Static Methods
    //================================================================================
    public static Recents load(String string) {
        Recents recents = new Recents();
        try {
            SequencedMap<String, Object> map = new Yaml().load(string);
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

    //================================================================================
    // Methods
    //================================================================================

    public String save() {
        if (isEmpty()) return "";
        StringBuilder sb = new StringBuilder("recents: \n");
        for (Recent recent : observableSet) {
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

    //================================================================================
    // Delegate Methods
    //================================================================================
    public boolean add(Recent recent) {
        return observableSet.add(recent);
    }

    public int size() {
        return observableSet.size();
    }

    public boolean isEmpty() {
        return observableSet.isEmpty();
    }

    //================================================================================
    // Getters
    //================================================================================
    public ObservableSet<Recent> observableSet() {
        return observableSet;
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    public record Recent(Path file, long timestamp) implements Comparable<Recent> {
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

        @Override
        public int compareTo(Recent o) {
            int cmp = Long.compare(timestamp, o.timestamp);
            if (cmp == 0) cmp = file.compareTo(o.file);
            return cmp;
        }

        @Override
        public String toString() {
            return "Recent{" +
                   "file=" + file +
                   ", timestamp=" + timestamp +
                   '}';
        }
    }
}
