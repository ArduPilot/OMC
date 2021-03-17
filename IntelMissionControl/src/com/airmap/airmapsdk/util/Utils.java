package com.airmap.airmapsdk.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

// Bogus class
public class Utils {
    public static String getStagingUrl() {
        return null;
    }

    public static String getTelemetryDebugUrl() {
        return null;
    }

    public static String getMqttDebugUrl() {
        return null;
    }

    public static String getIso8601StringFromDate(Date datetime) {
        return  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());

    }
}
