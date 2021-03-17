/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.test.rules;

import de.saxsys.mvvmfx.MvvmFX;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.ResourceBundle;

public class ResourceBundleInitializer implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ResourceBundle bundle = ResourceBundle
                    .getBundle("com/intel/missioncontrol/IntelMissionControl");
                MvvmFX.setGlobalResourceBundle(bundle);
                statement.evaluate();
            }
        };
    }
}
