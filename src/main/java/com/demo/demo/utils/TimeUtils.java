package com.demo.demo.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TimeUtils {

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
        DateTimeFormatter.ofPattern("h:mma").withLocale(Locale.ROOT),
        DateTimeFormatter.ofPattern("hh:mma").withLocale(Locale.ROOT),
        DateTimeFormatter.ofPattern("H:mm").withLocale(Locale.ROOT),
        DateTimeFormatter.ofPattern("HH:mm").withLocale(Locale.ROOT)
    );

    public static LocalTime parseTime(String time) {
        if (Objects.isNull(time) || time.isBlank()) {
            return null;
        }
        String rawTime = time.trim().toUpperCase(Locale.ROOT);
        
        if (rawTime.matches("^\\d{1,2}[AP]M$")) {
            rawTime = rawTime.replace("AM", ":00AM").replace("PM", ":00PM");
        }
        
        for (DateTimeFormatter dateTimeFormatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(rawTime, dateTimeFormatter);
            } catch (DateTimeParseException e) {
                // ignored
            }
        }
        
        throw new IllegalArgumentException("Unrecognized time format: " + time);
        
    }
}
