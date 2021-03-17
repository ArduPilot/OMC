/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.intel.missioncontrol.project.FlightPlan;
import eu.mavinci.core.obfuscation.IKeepClassname;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.flightplan.Flightplan;
import java.io.File;

public interface IFlightplanExporter extends IKeepClassname {

    void export(FlightPlan flightplan, File target, IMProgressMonitor progressMonitor);

    @Deprecated
    void exportLegacy(Flightplan flightplan, File target, IMProgressMonitor progressMonitor);

}
