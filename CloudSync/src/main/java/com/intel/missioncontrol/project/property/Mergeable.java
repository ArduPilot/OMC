/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public interface Mergeable<T> {

    /**
     * Merges the new value with the current value. If a merge conflict occurs, the specified {@link MergeStrategy}
     * determines how the conflict will be handled.
     */
    void merge(T newValue, MergeStrategy strategy);

    /**
     * Indicates whether any of this object's properties was changed since the last call to {@link
     * Mergeable#merge(Object, MergeStrategy)}.
     */
    boolean isDirty();

    static <T0 extends Mergeable, S0> BiConsumer<T0, S0> merge(MergeStrategy strategy) {
        return new BiConsumer<T0, S0>() {
            @Override
            public void accept(T0 t0, S0 s0) {
                t0.merge(s0, strategy);
            }
        };
    }

    static boolean isDirty(Mergeable m0) {
        return m0.isDirty();
    }

    static boolean isDirty(Mergeable m0, Mergeable m1) {
        return m0.isDirty() || m1.isDirty();
    }

    static boolean isDirty(Mergeable m0, Mergeable m1, Mergeable m2) {
        return m0.isDirty() || m1.isDirty() || m2.isDirty();
    }

    static boolean isDirty(Mergeable m0, Mergeable m1, Mergeable m2, Mergeable m3) {
        return m0.isDirty() || m1.isDirty() || m2.isDirty() || m3.isDirty();
    }

    static boolean isDirty(Mergeable m0, Mergeable m1, Mergeable m2, Mergeable m3, Mergeable m4) {
        return m0.isDirty() || m1.isDirty() || m2.isDirty() || m3.isDirty() || m4.isDirty();
    }

    static boolean isDirty(Mergeable m0, Mergeable m1, Mergeable m2, Mergeable m3, Mergeable m4, Mergeable m5) {
        return m0.isDirty() || m1.isDirty() || m2.isDirty() || m3.isDirty() || m4.isDirty() || m5.isDirty();
    }

    static boolean isDirty(
            Mergeable m0, Mergeable m1, Mergeable m2, Mergeable m3, Mergeable m4, Mergeable m5, Mergeable m6) {
        return m0.isDirty()
            || m1.isDirty()
            || m2.isDirty()
            || m3.isDirty()
            || m4.isDirty()
            || m5.isDirty()
            || m6.isDirty();
    }

    static boolean isDirty(
            Mergeable m0,
            Mergeable m1,
            Mergeable m2,
            Mergeable m3,
            Mergeable m4,
            Mergeable m5,
            Mergeable m6,
            Mergeable m7) {
        return m0.isDirty()
            || m1.isDirty()
            || m2.isDirty()
            || m3.isDirty()
            || m4.isDirty()
            || m5.isDirty()
            || m6.isDirty()
            || m7.isDirty();
    }

    static boolean isDirty(
            Mergeable m0,
            Mergeable m1,
            Mergeable m2,
            Mergeable m3,
            Mergeable m4,
            Mergeable m5,
            Mergeable m6,
            Mergeable m7,
            Mergeable m8) {
        return m0.isDirty()
            || m1.isDirty()
            || m2.isDirty()
            || m3.isDirty()
            || m4.isDirty()
            || m5.isDirty()
            || m6.isDirty()
            || m7.isDirty()
            || m8.isDirty();
    }

    static boolean isDirty(
            Mergeable m0,
            Mergeable m1,
            Mergeable m2,
            Mergeable m3,
            Mergeable m4,
            Mergeable m5,
            Mergeable m6,
            Mergeable m7,
            Mergeable m8,
            Mergeable m9) {
        return m0.isDirty()
            || m1.isDirty()
            || m2.isDirty()
            || m3.isDirty()
            || m4.isDirty()
            || m5.isDirty()
            || m6.isDirty()
            || m7.isDirty()
            || m8.isDirty()
            || m9.isDirty();
    }

    static boolean isDirty(
            Mergeable m0,
            Mergeable m1,
            Mergeable m2,
            Mergeable m3,
            Mergeable m4,
            Mergeable m5,
            Mergeable m6,
            Mergeable m7,
            Mergeable m8,
            Mergeable m9,
            Mergeable m10) {
        return m0.isDirty()
            || m1.isDirty()
            || m2.isDirty()
            || m3.isDirty()
            || m4.isDirty()
            || m5.isDirty()
            || m6.isDirty()
            || m7.isDirty()
            || m8.isDirty()
            || m9.isDirty()
            || m10.isDirty();
    }

    static boolean isDirty(
            Mergeable m0,
            Mergeable m1,
            Mergeable m2,
            Mergeable m3,
            Mergeable m4,
            Mergeable m5,
            Mergeable m6,
            Mergeable m7,
            Mergeable m8,
            Mergeable m9,
            Mergeable m10,
            Mergeable m11) {
        return m0.isDirty()
            || m1.isDirty()
            || m2.isDirty()
            || m3.isDirty()
            || m4.isDirty()
            || m5.isDirty()
            || m6.isDirty()
            || m7.isDirty()
            || m8.isDirty()
            || m9.isDirty()
            || m10.isDirty()
            || m11.isDirty();
    }

}
