/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TrackingAsyncDoublePropertyTest {

    @Test
    void DryRunStrategy_Detects_Merge_Conflict() {
        TrackingAsyncDoubleProperty prop0 = new TrackingAsyncDoubleProperty(this);
        var strategy = new MergeStrategy.DryRun();

        // OK: we didn't change the value of prop0 yet, so we take 'their' value.
        prop0.merge(1, strategy);
        assertEquals(0, strategy.getConflicts().size());

        // OK: We set 'our' value to 2, so no conflict with 'their' value.
        prop0.set(2);
        prop0.merge(2, strategy);
        assertEquals(0, strategy.getConflicts().size());

        // CONFLICT: both 'our' and 'their' value was changed.
        prop0.merge(3, strategy);
        assertEquals(1, strategy.getConflicts().size());
    }

    @Test
    void KeepOursStrategy_Resolves_Conflicts() {
        TrackingAsyncDoubleProperty prop0 = new TrackingAsyncDoubleProperty(this);
        var strategy = new MergeStrategy.KeepOurs();

        // OK: we didn't change the value of prop0 yet, so we take 'their' value.
        prop0.merge(1, strategy);
        assertEquals(1, prop0.get());

        // CONFLICT: both 'our' and 'their' value was changed, so we take 'our' value.
        prop0.set(2);
        prop0.merge(3, strategy);
        assertEquals(2, prop0.get());
    }

    @Test
    void KeepTheirsStrategy_Resolves_Conflicts() {
        TrackingAsyncDoubleProperty prop0 = new TrackingAsyncDoubleProperty(this);
        var strategy = new MergeStrategy.KeepTheirs();

        // OK: we didn't change the value of prop0 yet, so we take 'their' value.
        prop0.merge(1, strategy);
        assertEquals(1, prop0.get());

        // CONFLICT: both 'our' and 'their' value was changed, so we take 'their' value.
        prop0.set(2);
        prop0.merge(3, strategy);
        assertEquals(3, prop0.get());
    }

}
