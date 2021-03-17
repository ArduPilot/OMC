/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

/** check D-05: not enough space available */
public class AvailableSpaceToCopyValidator extends MatchingValidatorBase {
    public interface Factory {
        AvailableSpaceToCopyValidator create(Matching flightPlan);
    }

    public static final String VALIDATOR_NOT_ENOUGH_SPACE =
        "com.intel.missioncontrol.ui.validation.FilesSizeValidator.notEnoughSpace";

    private static final double SIZE_THUMBNAIL_MULTIPLIER = 1.01; // 1% for thumbnail images
    private static final long SIZE_CONFIG_SUMMAND = 5 * FileUtils.ONE_MB; // 5 MB spare, for dataset.pmt etc.;
    private static final long SIZE_BUFFER = FileUtils.ONE_GB * 1; // add 1 GB buffer;

    private final ILanguageHelper languageHelper;
    private final String className = ExifValidator.class.getName();

    @Inject
    public AvailableSpaceToCopyValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted Matching matching) {
        super(matching, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        addDependencies(
            matching.statusProperty(), matching.toImportImagesProperty(), matching.toImportTargetFolderProperty());
    }

    @Override
    protected boolean onInvalidated(Matching matching) {
        if (matching.getStatus() != MatchingStatus.NEW) {
            return true;
        }

        ObservableList<File> source = matching.toImportImagesProperty();
        ObservableValue<File> receiverDir = matching.toImportTargetFolderProperty();
        File targetDir = receiverDir.getValue();

        long size = source.stream().mapToLong(File::length).sum();
        size = (long)(size * SIZE_THUMBNAIL_MULTIPLIER + SIZE_CONFIG_SUMMAND + SIZE_BUFFER);
        if (size == 0 || targetDir == null) {
            return true;
        }

        long free = targetDir.getFreeSpace();
        if (free < size) {
            addWarning(
                languageHelper.getString(VALIDATOR_NOT_ENOUGH_SPACE, formatBytes(size), formatBytes(free)),
                ValidationMessageCategory.BLOCKING);
        }

        return true;
    }
}
