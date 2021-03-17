/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.test.rules;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.settings.ExpertSettings;
import eu.mavinci.desktop.gui.wwext.WWFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class EarthElevationModelInitializer implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WWFactory.configWW(StaticInjector.getInstance(ExpertSettings.class));
                // EarthElevationModel.init();
                // TODO FIXME initialize new elevation model
                base.evaluate();
            }
        };
    }
}
