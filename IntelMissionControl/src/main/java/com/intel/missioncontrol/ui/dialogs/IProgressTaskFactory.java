/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.api.support.ErrorCategory;
import com.intel.missioncontrol.api.support.Priority;
import eu.mavinci.plane.IAirplane;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IProgressTaskFactory {
    ProgressTask getForSendSupport(
            ErrorCategory category,
            Priority priority,
            String problemDescription,
            Map<String, Boolean> options,
            List<File> files,
            List<String> recipients,
            List<MatchingsTableRowData> matchingsUsage,
            long size,
            String fullName,
            String country,
            String ticketIdOld);

    ProgressTask getForResendSupport(File reportFolder, long size);

    ProgressTask getForTicketDownload(String ticketId, IApplicationContext applicationContext);

    ProgressTask getForGpsDebuggingDownload(IAirplane airplane);

    ProgressTask getForGpsRawDataDownload(IAirplane airplane);

    ProgressTask getForFlightplanDownload(IAirplane airplane);

    ProgressTask getForPhotologDownload(IAirplane airplane);

}
