/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class Dump extends CDump {

    public static final String KEY = "eu.mavinci.flightplan.Dump";
    public static final String KEY_Body = KEY + ".body";
    public static final String KEY_TO_STRING = KEY + ".toString";

    public Dump(String body) {
        super(body);
    }

    public Dump(String body, IFlightplanContainer parent) {
        super(body, parent);
    }

    public Dump(Dump source) {
        super(source.body);
    }

    public String toString() {
        return DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(KEY_TO_STRING, body);
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new Dump(this);
    }
}
