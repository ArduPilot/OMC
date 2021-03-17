/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.warnings;

import com.google.inject.Inject;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.validation.IValidationService;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;

public class UnresolvedWarningsDialogViewModel extends WarningsViewModel<Boolean> {

    private final org.apache.logging.log4j.Logger logger =
        LogManager.getLogger(UnresolvedWarningsDialogViewModel.class);
    private final StringProperty comment = new SimpleStringProperty();
    private final BooleanProperty ignoreWarnings = new SimpleBooleanProperty();
    private final BooleanProperty showDisclaimer = new SimpleBooleanProperty();
    private final BooleanProperty needsExtraConfirmation = new SimpleBooleanProperty();
    private final Command proceedCommand;

    @Inject
    public UnresolvedWarningsDialogViewModel(
            IValidationService validationService,
            INavigationService navigationService,
            ILanguageHelper languageHelper,
            ILinkBoxConnectionService linkBoxConnectionService) {
        super(validationService, navigationService, linkBoxConnectionService);

        proceedCommand =
            new DelegateCommand(
                () -> {
                    int i = 0;
                    for (WarningItemViewModel item : importantWarningsProperty()) {
                        i++;
                        logger.info(
                            languageHelper.getString(
                                UnresolvedWarningsDialogViewModel.class.getName() + ".logWarn",
                                i,
                                item.messageProperty().get()));
                    }

                    logger.info(
                        languageHelper.getString(
                            UnresolvedWarningsDialogViewModel.class.getName() + ".logMessage",
                            importantWarningsProperty().size(),
                            comment.get()));
                    setDialogResult(true);
                    getCloseCommand().execute();
                },
                ignoreWarnings
                    .and(comment.isNotEmpty())
                    .or(importantWarningsProperty().emptyProperty())
                    .or(needsExtraConfirmation.not()));

        setDialogResult(false);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        switch (getNavigationService().getWorkflowStep()) {
        case NONE:
        case DATA_PREVIEW:
            showDisclaimer.set(false);
            needsExtraConfirmation.set(false);
            break;
        case FLIGHT:
        case PLANNING:
            showDisclaimer.set(true);
            needsExtraConfirmation.set(true);
            break;
        }
    }

    public BooleanProperty showDisclaimerProperty() {
        return showDisclaimer;
    }

    public BooleanProperty needsExtraConfirmationProperty() {
        return needsExtraConfirmation;
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public BooleanProperty ignoreWarningsProperty() {
        return ignoreWarnings;
    }

    public Command getProceedCommand() {
        return proceedCommand;
    }

}
