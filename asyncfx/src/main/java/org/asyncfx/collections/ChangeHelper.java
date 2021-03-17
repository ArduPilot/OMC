/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.Arrays;
import java.util.List;
import org.asyncfx.PublishSource;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
class ChangeHelper {

    static String addRemoveChangeToString(int from, int to, List<?> added, List<?> removed) {
        StringBuilder b = new StringBuilder();

        if (removed.isEmpty()) {
            b.append(added);
            b.append(" added at ").append(from);
        } else {
            b.append(removed);
            if (from == to) {
                b.append(" removed at ").append(from);
            } else {
                b.append(" replaced by ");
                b.append(added);
                b.append(" at ").append(from);
            }
        }

        return b.toString();
    }

    static String permChangeToString(int[] permutation) {
        return "permutated by " + Arrays.toString(permutation);
    }

    static String updateChangeToString(int from, int to) {
        return "updated at range [" + from + ", " + to + ")";
    }

}
