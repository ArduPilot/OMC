/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;

public class WorkingHorse {

    public interface Factory {
        WorkingHorse create();
    }

    private final IPathProvider pathProvider;

    @Inject
    WorkingHorse(IPathProvider pathProvider) {
        this.pathProvider = pathProvider;
    }

    public void doSomething() {
        System.out.println(pathProvider.getSettingsDirectory());
    }
}
