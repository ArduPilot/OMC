/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.warnings;

import com.google.inject.Inject;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.validation.IValidationService;

public class WarningsPopoverViewModel extends WarningsViewModel<Void> {

    @Inject
    public WarningsPopoverViewModel(
            IValidationService validationService,
            INavigationService navigationService,
            ILinkBoxConnectionService linkBoxConnectionService) {
        super(validationService, navigationService, linkBoxConnectionService);
        warningsProperty()
            .emptyProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue) {
                        getCloseCommand().execute();
                        // TODO FIXME... this isnt closing the popover....
                    }
                });
    }

}
