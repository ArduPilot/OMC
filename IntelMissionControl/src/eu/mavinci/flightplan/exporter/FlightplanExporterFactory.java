/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.ui.validation.IValidationService;
import eu.mavinci.desktop.gui.doublepanel.planemain.FlightplanExportTypes;
import eu.mavinci.desktop.helper.MFileFilter;
import java.util.Map;

public class FlightplanExporterFactory implements IFlightplanExporterFactory {

    private IScreenshotManager screenshotManager;
    private ILanguageHelper languageHelper;
    private IWWGlobes globes;
    private IValidationService validationService;
    private IApplicationContext applicationContext;

    private final Map<FlightplanExportTypes, IFlightplanExporterFactory> factoryMap =
        ImmutableMap.<FlightplanExportTypes, IFlightplanExporterFactory>builder()
            .put(
                FlightplanExportTypes.KML,
                () -> new DefaultFlightplanExporter("eu/mavinci/core/xml/flightplankml.xsl", MFileFilter.kmlFilter))
            .put(
                FlightplanExportTypes.CSV,
                () -> new DefaultFlightplanExporter("eu/mavinci/core/xml/toCsv.xsl", MFileFilter.csvFpExportFilter))
            .put(FlightplanExportTypes.LCSV, () -> new LitchiCsvExporter())
            .put(
                FlightplanExportTypes.GPX,
                () -> new DefaultFlightplanExporter("eu/mavinci/core/xml/toGpx.xsl", MFileFilter.gpxFpExportFilter))
            .put(
                FlightplanExportTypes.RTE,
                () -> new DefaultFlightplanExporter("eu/mavinci/core/xml/toRte.xsl", MFileFilter.rteFpExportFilter))
            .put(
                FlightplanExportTypes.FPL,
                () -> new DefaultFlightplanExporter("eu/mavinci/core/xml/toFpl.xsl", MFileFilter.fplFpExportFilter))
            .put(
                FlightplanExportTypes.ASCTECCSVJPG,
                () -> new AscTecCsvJpgExporter(globes, validationService, screenshotManager, languageHelper))
            .put(FlightplanExportTypes.ASCTECCSV, () -> new AscTecCsvExporter(globes, validationService))
            .put(
                FlightplanExportTypes.ANP,
                () -> new AcpExporter(globes, validationService, screenshotManager, languageHelper, applicationContext))
            .put(
                FlightplanExportTypes.ACP,
                () -> new AcpExporter(globes, validationService, screenshotManager, languageHelper, applicationContext))
            .build();

    @Inject
    public FlightplanExporterFactory(
            IWWGlobes globes,
            IValidationService validationService,
            IScreenshotManager screenshotManager,
            ILanguageHelper languageHelper,
            IApplicationContext applicationContext) {
        this.screenshotManager = screenshotManager;
        this.languageHelper = languageHelper;
        this.globes = globes;
        this.validationService = validationService;
        this.applicationContext = applicationContext;
    }

    public IFlightplanExporter createExporter(FlightplanExportTypes type) {
        IFlightplanExporterFactory factory = factoryMap.get(type);
        if (factory == null) {
            throw new IllegalArgumentException();
        }

        return factory.create();
    }

    private interface IFlightplanExporterFactory {
        IFlightplanExporter create();
    }
}
