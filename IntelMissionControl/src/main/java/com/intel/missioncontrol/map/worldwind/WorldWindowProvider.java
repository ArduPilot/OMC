/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import gov.nasa.worldwind.WorldWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WorldWindowProvider {

    private final List<Consumer<WorldWindow>> consumers = new ArrayList<>();
    private WorldWindow worldWindow;

    public void whenAvailable(Consumer<WorldWindow> consumer) {
        if (worldWindow != null) {
            consumer.accept(worldWindow);
        } else {
            consumers.add(consumer);
        }
    }

    public void provide(WorldWindow worldWindow) {
        if (this.worldWindow != null) {
            throw new IllegalStateException("WorldWindow has already been provided.");
        }

        this.worldWindow = worldWindow;

        for (Consumer<WorldWindow> consumer : consumers) {
            consumer.accept(worldWindow);
        }
    }

}
