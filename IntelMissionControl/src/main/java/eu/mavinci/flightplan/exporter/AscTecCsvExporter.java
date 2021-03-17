/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.google.inject.Inject;
import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.ui.validation.IValidationService;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.asctec.ACPWriterWrapper;
import gov.nasa.worldwind.globes.Globe;
import java.io.File;
import java.io.IOException;
import org.slf4j.LoggerFactory;

public class AscTecCsvExporter implements IFlightplanExporter {

    private final Globe globe;
    private final IValidationService validationService;

    @Inject
    public AscTecCsvExporter(IWWGlobes globes, IValidationService validationService) {
        this.globe = globes.getDefaultGlobe();
        this.validationService = validationService;
    }

    public void exportLegacy(Flightplan flightplan, File target, IMProgressMonitor progressMonitor) {
        String xsl = AscTecCsvHelper.getXsl(flightplan);
        File flightPlanFolder = flightplan.getFile().getParentFile();
        ACPWriterWrapper acpWriter =
            new ACPWriterWrapper(
                globe, validationService, null, target, null, flightplan.getSector(), flightplan.getName());
        acpWriter.setProgressMonitor(progressMonitor);
        try {
            acpWriter.flightplanToJpg(flightplan, flightPlanFolder, xsl, false, true);
        } catch (IOException e) {
            LoggerFactory.getLogger(AscTecCsvExporter.class).error(e.getMessage(), e);
        }
    }

    @Override
    public void export(FlightPlan flightplan, File target, IMProgressMonitor progressMonitor) {
        throw new NotImplementedException();
    }
}
