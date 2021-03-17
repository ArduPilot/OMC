/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.google.inject.Inject;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.desktop.helper.FileHelper;

public class FileExtractor implements IFileExtractor {

    @Inject
    public FileExtractor(IPathProvider pathProvider, ILanguageHelper languageHelper, ILicenceManager licenceManager) {
        // init hw descriptions
        FileHelper.copyJarFolderToDiskInclAsking(
            languageHelper,
            licenceManager,
            "com/intel/missioncontrol/descriptions/lenses/",
            pathProvider.getLensDescriptionsDirectory().toFile(),
                licenceManager.getActiveLicence().getEditionList());
        FileHelper.copyJarFolderToDiskInclAsking(
            languageHelper,
            licenceManager,
            "com/intel/missioncontrol/descriptions/cameras/",
            pathProvider.getCameraDescriptionsDirectory().toFile(),
                licenceManager.getActiveLicence().getEditionList());
        FileHelper.copyJarFolderToDiskInclAsking(
            languageHelper,
            licenceManager,
            "com/intel/missioncontrol/descriptions/platforms/",
            pathProvider.getPlatformDescriptionsDirectory().toFile(),
                licenceManager.getActiveLicence().getEditionList());

        // init templates
        FileHelper.copyJarFolderToDiskInclAsking(
            languageHelper,
            licenceManager,
            "com/intel/missioncontrol/templates/",
            pathProvider.getTemplatesDirectory().toFile(),
                licenceManager.getActiveLicence().getEditionList());

        // init bundles openAirspace files
        FileHelper.copyJarFolderToDiskInclAsking(
            languageHelper,
            licenceManager,
            "com/intel/missioncontrol/airspaces/bundles/openair/",
            pathProvider.getLocalAirspacesFolder().toFile(),
            null);
    }

}
