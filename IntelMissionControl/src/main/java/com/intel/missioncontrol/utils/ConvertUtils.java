/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import com.ibm.icu.text.SimpleDateFormat;

import java.util.Date;

public class ConvertUtils {

    /**
     * Convert long date to String.
     *
     * @param dateLong - date in long format
     * @return - String yyyy-MM-dd HH:mm:ss
     */
    public static String convertLongToDate(long dateLong, String dateFormat) {
        SimpleDateFormat dateDormat = new SimpleDateFormat(dateFormat);

        return dateDormat.format(new Date(dateLong));
    }

}
