/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

public enum ExportTypes {
    CSV("com.intel.missioncontrol.ui.analysis.AnalysisView.csvExport"),
    KML("com.intel.missioncontrol.ui.analysis.AnalysisView.kmlExport"),
    AGISOFT_PHOTOSCAN("com.intel.missioncontrol.ui.analysis.AnalysisView.agiSoftPhotoScanExport"),
    AGISOFT_METASHAPE("com.intel.missioncontrol.ui.analysis.AnalysisView.agiSoftMetashapeExport"),
    CONTEXT_CAPTURE("com.intel.missioncontrol.ui.analysis.AnalysisView.contextCaptureExport"),
    PIX4D_DESKTOP("com.intel.missioncontrol.ui.analysis.AnalysisView.pix4DesktopExport"),
    WRITE_EXIF("com.intel.missioncontrol.ui.analysis.AnalysisView.writeExif"),
    INTEL_INSIGHT_PROCESS("com.intel.missioncontrol.ui.analysis.AnalysisView.intelInsightUploadProcessing"),
    INTEL_INSIGHT_NOTPROCESS("com.intel.missioncontrol.ui.analysis.AnalysisView.intelInsightUploadNotProcessing");

    public final String key;

    ExportTypes(String key) {
        this.key = key;
    }
}
