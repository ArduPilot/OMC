/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.asctec;

import com.intel.missioncontrol.ui.validation.IValidationService;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import java.awt.image.BufferedImage;
import java.io.File;

public class ACPWriterWrapper extends ACPWriter {

    private File target;
    private File targetCsv;

    public ACPWriterWrapper(
            Globe globe,
            IValidationService validationService,
            File target,
            File targetCsv,
            BufferedImage screenshot,
            Sector jpgSector,
            String flightplanName) {
        super(globe, validationService, screenshot, jpgSector, flightplanName);
        this.target = target;
        this.targetCsv = targetCsv;
    }

    @Override
    public File getTarget() {
        return target;
    }

    @Override
    public File getTargetCsv() {
        return targetCsv;
    }
}
