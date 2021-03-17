/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.support.ErrorCategory;
import com.intel.missioncontrol.api.support.Priority;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.controlsfx.dialog.ProgressDialog;

@Deprecated
public interface IVeryUglyDialogHelper {

    ProgressDialog createProgressDialogForResendSupport(File reportFolder, long filesSize);

    ProgressDialog createProgressDialogForSendSupport(
            ErrorCategory category,
            Priority priority,
            String problemDescription,
            Map<String, Boolean> options,
            List<File> files,
            long filesSize,
            List<String> recipients,
            List<MatchingsTableRowData> matchingsUsage,
            String fullName,
            String country,
            String ticketIdOld);

    ProgressDialog createProgressDialogForTicketDownload(String ticketId, IApplicationContext applicationContext);

    ProgressDialog createProgressDialogFromTask(ProgressTask task, String title, String header);

}
