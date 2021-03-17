/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api;

import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.ui.sidepane.analysis.ExportTypes;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IExportService {

    boolean isNotExportedAsCsv(Matching matching);

    void exportAsCsv(Matching matching, MSpatialReference srs) throws IOException;

    boolean isNotExportedAsPix4d(Matching matching);

    void exportAsPix4d(Matching matching, MSpatialReference srs) throws Exception;

    void openInPix4d(Matching matching);

    boolean isNotExportedToAgiSoftPhotoScan(Matching matching);

    void openInAgiSoft(Matching matching) throws IOException;

    void writeDebugAgisoftScript() throws Exception;

    void exportToAgiSoftPhotoScan(Matching matching, MSpatialReference srs) throws Exception;

    boolean isNotExportedToContextCapture(Matching matching);

    void openInContextCapture(Matching matching);

    void exportToContextCapture(Matching matching, MSpatialReference srs) throws Exception;

    boolean isWriteExifDataAllowed(Matching matching);

    void writeExifData(
            Consumer<IBackgroundTaskManager.ProgressStageFirer> firer,
            BooleanSupplier cancelIndicator,
            Matching matching)
            throws Exception;

    Function<Matching, String> getTagretFilePath(ExportTypes type);
}
