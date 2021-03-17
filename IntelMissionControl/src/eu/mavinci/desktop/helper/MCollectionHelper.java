/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import java.util.List;

public class MCollectionHelper {
    @SuppressWarnings("unchecked")
    public static void permuteNeighbours(@SuppressWarnings("rawtypes") List list) {
        for (int k = 0; k < list.size(); k += 2) {
            Object t = list.get(k);
            list.set(k, list.get(k + 1));
            list.set(k + 1, t);
        }
    }

}
