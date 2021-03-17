/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.MissionManager;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.api.support.SupportManager;
import org.junit.Before;

/**
 * Base test class to be extended when we want to use Guice dependency injection. For each new class that has to be
 * tested a binding should be added in the configure() method.
 *
 * @author aiacovici
 */
public class TestBase {

    protected Injector injector =
        Guice.createInjector(
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(IMissionManager.class).to(MissionManager.class);
                    bind(ISupportManager.class).to(SupportManager.class);
                }
            });

    @Before
    public void setup() {
        injector.injectMembers(this);
    }
}
