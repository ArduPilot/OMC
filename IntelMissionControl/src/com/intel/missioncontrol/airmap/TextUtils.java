/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import java.util.Iterator;

public final class TextUtils {
    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    public static String join(String delim, Object[] objs) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object o: objs) {
            if (first) {
                first = false;
            } else {
                sb.append(delim);
            }
            sb.append(o);
        }
        return sb.toString();
    }

    public static String join(CharSequence delim, Iterable objs) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = objs.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(delim);
                sb.append(it.next());
            }
        }
        return sb.toString();
    }}
