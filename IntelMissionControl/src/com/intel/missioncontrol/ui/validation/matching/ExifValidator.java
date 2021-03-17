/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;

/** check D-03: Hardware selection of matching matches to exif metadata of images */
public class ExifValidator extends MatchingValidatorBase {
    public interface Factory {
        ExifValidator create(Matching flightPlan);
    }

    private final String className = ExifValidator.class.getName();
    ILanguageHelper languageHelper;
    private final IApplicationContext applicationContext;

    @Inject
    public ExifValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            IApplicationContext applicationContext,
            @Assisted Matching matching) {
        super(matching, quantityStyleProvider);
        this.applicationContext = applicationContext;
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        addDependencies(matching.exifDataMsgProperty(), matching.statusProperty());
    }

    @Override
    protected boolean onInvalidated(Matching matching) {
        if (matching.getStatus() != MatchingStatus.IMPORTED) {
            return false;
        }

        String msg = matching.exifDataMsgProperty().get();

        if (msg != null) {
            SimpleResolveAction action =
                new SimpleResolveAction(
                    languageHelper.getString(className + ".resolve"),
                    () -> {
                        if (!matching.detectBestHwConfiguration()) {
                            applicationContext.addToast(
                                Toast.of(ToastType.ALERT)
                                    .setText(languageHelper.getString(className + ".resolveFailed"))
                                    .create());
                        }
                    });

            addWarning(msg, ValidationMessageCategory.NORMAL, action);
        }

        return true;
    }

}
