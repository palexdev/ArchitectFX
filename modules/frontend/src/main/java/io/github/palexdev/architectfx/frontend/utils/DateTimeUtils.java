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
}
