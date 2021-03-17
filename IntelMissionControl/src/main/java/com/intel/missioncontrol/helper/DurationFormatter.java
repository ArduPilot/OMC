/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import java.time.Duration;

public class DurationFormatter {

    private boolean includeDays = true;
    private boolean includeHours = true;
    private boolean includeMinutes = true;
    private boolean includeSeconds = true;

    public DurationFormatter() {}

    public DurationFormatter(
            boolean includeDays, boolean includeHours, boolean includeMinutes, boolean includeSeconds) {
        this.includeDays = includeDays;
        this.includeHours = includeHours;
        this.includeMinutes = includeMinutes;
        this.includeSeconds = includeSeconds;
    }

    public void setIncludeDays(boolean value) {
        includeDays = value;
    }

    public void setIncludeHours(boolean value) {
        includeHours = value;
    }

    public void setIncludeMinutes(boolean value) {
        includeMinutes = value;
    }

    public void setIncludeSeconds(boolean value) {
        includeSeconds = value;
    }

    public String format(Duration duration) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder stringBuilder = new StringBuilder();

        if (includeDays) {
            stringBuilder.append(days);
            stringBuilder.append(":");
        }

        if (includeHours) {
            stringBuilder.append(String.format("%02d:", hours));
        } else if (stringBuilder.length() > 0) {
            stringBuilder.append("00:");
        }

        if (includeMinutes) {
            stringBuilder.append(String.format("%02d:", minutes));
        } else if (stringBuilder.length() > 0) {
            stringBuilder.append("00:");
        }

        if (includeSeconds) {
            stringBuilder.append(String.format("%02d", seconds));
        } else if (stringBuilder.length() > 0) {
            stringBuilder.append("00");
        }

        return stringBuilder.toString();
    }

}
