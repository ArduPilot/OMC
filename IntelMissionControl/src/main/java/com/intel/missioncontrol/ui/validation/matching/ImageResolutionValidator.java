/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerMatch;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.helper.ImageHelper;
import java.awt.Dimension;
import java.io.File;

/** check D-06: If image resolution fits to selected hardware */
public class ImageResolutionValidator extends MatchingValidatorBase {
    public interface Factory {
        ImageResolutionValidator create(Matching flightPlan);
    }

    private final String className = ImageResolutionValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public ImageResolutionValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted Matching matching) {
        super(matching, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        addDependencies(
            matching.picturesCountProperty(),
            matching.hardwareConfigurationProperty(),
            matching.statusProperty(),
            matching.exifDataMsgProperty());
    }

    @Override
    protected boolean onInvalidated(Matching matching) {
        if (matching.getStatus() != MatchingStatus.IMPORTED) {
            return false;
        }

        if (matching.getLegacyMatching().getPicsLayer().sizeMapLayer() == 0) {
            return false;
        }

        IMapLayer layer = matching.getLegacyMatching().getPicsLayer().getMapLayer(0);

        if (layer instanceof MapLayerMatch) {
            MapLayerMatch match = (MapLayerMatch)layer;
            File image = match.getResourceFile();
            if (image != null) {
                Dimension dim = ImageHelper.getImageDimension(image);
                IGenericCameraDescription cam =
                    matching.getHardwareConfiguration()
                        .getPrimaryPayload(IGenericCameraConfiguration.class)
                        .getDescription();
                if (dim != null && (dim.width != cam.getCcdResX() || dim.height != cam.getCcdResY())) {
                    addWarning(
                        languageHelper.getString(
                            className + ".dimMismatch", dim.width, cam.getCcdResX(), dim.height, cam.getCcdResY()),
                        ValidationMessageCategory.NORMAL);
                }
            }
        }

        return true;
    }

}
