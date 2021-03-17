/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.google.inject.Inject;
import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.project.FlightPlan;
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
import org.apache.commons.io.FilenameUtils;
import org.asyncfx.concurrent.Future;

public class AscTecCsvJpgExporter implements IFlightplanExporter {
    private final Globe globe;
    private final IValidationService validationService;

    private final IScreenshotManager mapScreenshotManager;
    private final ILanguageHelper languageHelper;

    @Inject
    public AscTecCsvJpgExporter(
            IWWGlobes globes,
            IValidationService validationService,
            IScreenshotManager screenshotManager,
            ILanguageHelper languageHelper) {
        this.globe = globes.getDefaultGlobe();
        this.validationService = validationService;
        this.mapScreenshotManager = screenshotManager;
        this.languageHelper = languageHelper;
    }

    public void exportLegacy(Flightplan flightplan, File target, IMProgressMonitor progressMonitor) {
        // do a screenshot, on success continue writing a file
        Future<Pair<BufferedImage, Sector>> result =
            mapScreenshotManager.makeBackgroundScreenshotAsync(flightplan.getSector());
        Pair<BufferedImage, Sector> pair = null;
        try {
            pair = result.getUnchecked();
        } catch (Exception e) {
            progressMonitor.setNote(
                languageHelper.getString(IFlightplanExporter.class.getName() + ".screenshotFailed"));
            return;
        }

        try {
            writeFlightPlan(flightplan, target, progressMonitor, pair.getKey(), pair.getValue());
        } catch (IOException e) {
            progressMonitor.setNote(languageHelper.getString(IFlightplanExporter.class.getName() + ".exportFailed"));
        }
    }

    private void writeFlightPlan(
            Flightplan flightplan,
            File target,
            IMProgressMonitor progressMonitor,
            BufferedImage bufferedImage,
            Sector jpgSector)
            throws IOException {
        String xsl = AscTecCsvHelper.getXsl(flightplan);
        File flightPlanFolder = flightplan.getFile().getParentFile();
        ACPWriterWrapper acpWriter =
            new ACPWriterWrapper(
                globe,
                validationService,
                target,
                createCsvFile(target),
                bufferedImage,
                jpgSector,
                flightplan.getName());
        acpWriter.setProgressMonitor(progressMonitor);
        acpWriter.flightplanToJpg(flightplan, flightPlanFolder, xsl, true, true);
    }

    private File createCsvFile(File target) {
        String csvPathname = FilenameUtils.removeExtension(target.toString()).concat(".csv");
        return new File(csvPathname);
    }

    @Override
    public void export(FlightPlan flightplan, File target, IMProgressMonitor progressMonitor) {
        throw new NotImplementedException();
    }
}
