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

package io.github.palexdev.architectfx.frontend.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DateTimeUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    //================================================================================
    // Constructors
    //================================================================================
    private DateTimeUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static ZonedDateTime toDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static long epochMilli() {
        return Instant.now().toEpochMilli();
    }

    public static String modifiedToHuman(long millis) {
        long elapsed = epochMilli() - millis;
        long seconds = elapsed / 1000;
        if (seconds < 60) {
            return "Modified a few seconds ago";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return "Modified %d minute%s ago".formatted(minutes, minutes == 1 ? "" : "s");
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return "Modified %d hour%s ago".formatted(hours, hours == 1 ? "" : "s");
        }

        long days = hours / 24;
        return "Modified %d day%s ago".formatted(days, days == 1 ? "" : "s");
    }
}
