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

    private static final DateTimeFormatter TIME_FORMATTER_AM_PM = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Parses a time string into a LocalTime object.
     * Accepts formats like "2PM", "02:00PM", "14:00", etc.
     * Returns null if the input is null or blank.
     * @param time the time string to parse
     * @return the parsed LocalTime object or null
     * @throws IllegalArgumentException if the time format is unrecognized
     */
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
    
    /**
     * Formats a LocalTime object into a string in "hh:mm AM/PM" format.
     * @param time the LocalTime object to format
     * @return formatted time string
     */
    public static String formatTime(LocalTime time) {
        if (Objects.isNull(time)) {
            return null;
        }
        return time.format(TIME_FORMATTER_AM_PM);
    }
}
