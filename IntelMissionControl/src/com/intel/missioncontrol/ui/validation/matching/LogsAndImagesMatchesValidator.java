/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;

/** check D-01: If the number of logs fits to the number of images */
public class LogsAndImagesMatchesValidator extends MatchingValidatorBase {
    public interface Factory {
        LogsAndImagesMatchesValidator create(Matching flightPlan);
    }

    private final String className = LogsAndImagesMatchesValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public LogsAndImagesMatchesValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted Matching matching) {
        super(matching, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        addDependencies(
            matching.toImportTriggersCountProperty(),
            matching.toImportImagesCountProperty(),
            matching.statusProperty(),
            matching.toImportImageSourceFolderProperty());
    }

    @Override
    protected boolean onInvalidated(Matching matching) {
        if (matching.getStatus() != MatchingStatus.NEW) {
            return false;
        }

        if (matching.toImportTriggersCountProperty().get() != matching.toImportImagesCountProperty().get()
                && matching.toImportImageSourceFolderProperty().get() != null) {
            addWarning(
                languageHelper.getString(
                    className + ".triggerImageCountMismatch",
                    matching.toImportTriggersCountProperty().get(),
                    matching.toImportImagesCountProperty().get()),
                ValidationMessageCategory.NORMAL);
        }

        return true;
    }

}
