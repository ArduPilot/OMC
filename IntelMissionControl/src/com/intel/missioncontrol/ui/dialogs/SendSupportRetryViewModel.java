/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.google.inject.Inject;
import com.intel.missioncontrol.api.support.ISupportManager;
import com.intel.missioncontrol.api.support.SupportConstants;
import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.helper.MProperties;
import eu.mavinci.desktop.helper.FileHelper;
import java.io.File;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

/** Created by akorotenko on 7/19/17. */
public class SendSupportRetryViewModel extends DialogViewModel<Void, File> {

    private ObservableList<Pair<String, String>> reportDetails = FXCollections.observableArrayList();
    private ObjectProperty<File> reportFolder = new SimpleObjectProperty<>();

    private final ILanguageHelper languageHelper;
    private final ISupportManager supportManager;
    private final IVeryUglyDialogHelper dialogHelper;

    private long filesSize = 0;

    @Inject
    public SendSupportRetryViewModel(
            ILanguageHelper languageHelper, ISupportManager supportManager, IVeryUglyDialogHelper dialogHelper) {
        this.languageHelper = languageHelper;
        this.supportManager = supportManager;
        this.dialogHelper = dialogHelper;
    }

    public ObservableList<Pair<String, String>> getReportDetails() {
        return reportDetails;
    }

    @Override
    public void initializeViewModel(File reportFolder) {
        super.initializeViewModel(reportFolder);

        this.reportFolder.set(reportFolder);
        MProperties properties = supportManager.getReportProperties(reportFolder);
        reportDetails.add(
            new Pair(SupportConstants.KEY_PRIORITY, properties.getProperty(SupportConstants.KEY_PRIORITY)));
        reportDetails.add(new Pair(SupportConstants.KEY_DATE, properties.getProperty(SupportConstants.KEY_DATE)));
        reportDetails.add(
            new Pair(SupportConstants.KEY_STORED_COMMENT, properties.getProperty(SupportConstants.KEY_STORED_COMMENT)));
        filesSize =
            Optional.ofNullable(properties.getProperty(SupportConstants.KEY_SIZE)).map(Long::parseLong).orElse(0L);
        reportDetails.add(new Pair(SupportConstants.KEY_SIZE, FileUtils.byteCountToDisplaySize(filesSize)));
    }

    public void discard() {
        FileHelper.deleteDir(languageHelper, reportFolder.getValue(), true);
        supportManager.checkErrorReports();
        getCloseCommand().execute();
    }

    public void keepTrying() {
        dialogHelper.createProgressDialogForResendSupport(reportFolder.getValue(), filesSize);
        getCloseCommand().execute();
    }
}
