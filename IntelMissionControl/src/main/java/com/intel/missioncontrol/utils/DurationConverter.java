/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/** @author Vladimir Iordanov */
public class DurationConverter {
    private final SimpleDateFormat simpleDateFormat;

    public DurationConverter() {
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String formatToString(long value) {
        return simpleDateFormat.format(new Date(TimeUnit.MILLISECONDS.convert(value, TimeUnit.SECONDS)));
    }

    public int parseToSeconds(String valueStr) throws ParseException {
        return (int)TimeUnit.SECONDS.convert(simpleDateFormat.parse(valueStr).getTime(), TimeUnit.MILLISECONDS);
    }
}
