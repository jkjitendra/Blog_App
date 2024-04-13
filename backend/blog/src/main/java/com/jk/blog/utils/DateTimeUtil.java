package com.jk.blog.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String formatInstantToIsoString(Instant instant) {
        if (instant == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return formatter.format(instant);
    }
}
