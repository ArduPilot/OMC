/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.validation.IValidationService;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.asctec.ACPWriterWrapper;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.util.Pair;
import org.asyncfx.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcpExporter implements IFlightplanExporter {

    private final Globe globe;
    private final IValidationService validationService;
    private final IScreenshotManager mapScreenshotManager;
    private final ILanguageHelper languageHelper;
    private IApplicationContext applicationContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(AcpExporter.class);

    @Inject
    public AcpExporter(
            IWWGlobes globes,
            IValidationService validationService,
            IScreenshotManager screenshotManager,
            ILanguageHelper languageHelper,
            IApplicationContext applicationContext) {
        this.globe = globes.getDefaultGlobe();
        this.validationService = validationService;
        this.mapScreenshotManager = screenshotManager;
        this.languageHelper = languageHelper;
        this.applicationContext = applicationContext;
    }

    public void exportLegacy(Flightplan flightplan, File target, IMProgressMonitor progressMonitor) {
        // do a screenshot, on success continue writing a file
        Future<Pair<BufferedImage, Sector>> result =
            mapScreenshotManager.makeBackgroundScreenshotAsync(flightplan.getSector());
        Pair<BufferedImage, Sector> pair = null;
        try {
            pair = result.getUnchecked();
        } catch (Exception e) {
            LOGGER.error("screenshot failed", e);
            progressMonitor.setNote(
                languageHelper.getString(IFlightplanExporter.class.getName() + ".screenshotFailed"));
            applicationContext.addToast(
                Toast.of(ToastType.ALERT).setText("Can export map: " + e.getMessage()).setShowIcon(true).create());
            return;
        }

        try {
            writeFlightPlan(flightplan, target, progressMonitor, pair.getKey(), pair.getValue());
        } catch (IOException e) {
            LOGGER.error("export failed", e);
            progressMonitor.setNote(languageHelper.getString(IFlightplanExporter.class.getName() + ".exportFailed"));
            applicationContext.addToast(
                Toast.of(ToastType.ALERT).setText("Can not save file: " + e.getMessage()).setShowIcon(true).create());
        }
    }

    private void writeFlightPlan(
            Flightplan flightplan,
            File target,
            IMProgressMonitor progressMonitor,
            BufferedImage bufferedImage,
            Sector jpgSector)
            throws IOException {
        File flightPlanFolder = flightplan.getFile().getParentFile();
        ACPWriterWrapper acpWriter =
            new ACPWriterWrapper(
                globe, validationService, target, null, bufferedImage, jpgSector, flightplan.getName());
        acpWriter.setProgressMonitor(progressMonitor);
        acpWriter.flightplanToACP(flightplan, flightPlanFolder, null);
        progressMonitor.close();
    }

    @Override
    public void export(FlightPlan flightplan, File target, IMProgressMonitor progressMonitor) {
        throw new NotImplementedException();
    }
}
