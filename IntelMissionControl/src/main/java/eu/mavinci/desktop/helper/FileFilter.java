/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

/**
 * Bean to store filter name and extensions (duplicates MFileFilter)
 *
 * @author Vladimir Iordanov
 */
public enum FileFilter {
    ALL("Any file", "*.*"),
    TXT("Text file", "*.txt"),
    LOG("*.log"),
    TPO("*.tpo"),
    CONNECTOR_UPDATE("*.cbin"),
    MLF_UPDATE("*.mlf"),
    RPM_UPDATE("*.rpm"),
    APP_LOG("applog", "*.log"),
    EXE("*.exe"),
    ENSO_CAMERA_CAL("ensoCamCal", "*.cal"),
    ICAROS_CAMERA_CAL("icarosCamCal", "*.cam"),
    MENCI_CAMERA_CAL("menciCamCal", "*.cvcc"),
    AGISOFT_CAMERA_CAL("agisoftCamCal", "*.xml"),
    MAC_UPDATE("macUpdate", "*.dmg"),
    CSV_FP_EXPORT("csvFP", "*.csv"),
    RTE_FP_EXPORT("*.rte"),
    FPL_FP_EXPORT("*.fpl"),
    GPX_FP_EXPORT("*.gpx"),
    CSV_MATCHING_EXPORT("Comma-separated values", "*.csv"),
    PSZ_MATCHING_EXPORT("Agisoft Photoscan Project", "*.psz"),
    PSX_MATCHING_EXPORT("Agisoft Photoscan / Metashape Project", "*.psx"),
    BENTLEY_XML_MATCHING_EXPORT("Bentley XML", "*.xml"),
    PIX4D_MATCHING_EXPORT("pix4d Project", "*.p4d"),
    ACP_FP_EXPORT("Intel® Falcon 8+ teach-in project", "*.acp"),
    CSV_ASC_TEC_FP_EXPORT("AscTec CSV", "*.csv"),
    AP_UPDATE("apUpdate", "*.tgz"),
    TGZ("tgz", "*.tgz", "*.tar.gz"),
    KML("*.kml"),
    KMZ("*.kmz"),
    KML_KMZ("Keyhole Markup Language files", "*.kml", "*.kmz"),
    SHP("ESRIshape", "*.shp"),
    GEO_TIFF("GeoTIFF", "*.gtif", "*.tif", "*.tiff"),
    TIFF("TIFF Image", "*.tif", "*.tiff"),
    TPS("*.tps"),
    FML("*.fml"),
    FLG("*.flg"),
    VLG("*.vlg"),
    PNG("PNG Image", "*.png"),
    GIF("GIF Image", "*.gif"),
    FLG_ZIP("*.flg.zip"),
    VLG_ZIP("*.vlg.zip"),
    CONFIG("*.config"),
    CAMERA("*.camera"),
    AIRSPACE("airspace", "*.txt"),
    PHOTO_LOG("photoLog", "*.plg"),
    PHOTO_LOG_ZIP("Zipped photoLog", "*.plg.zip"),
    BBX("*.bbx"),
    BBX_ZIP("*.bbx.zip"),
    PMT("picMatch", "*.pmt"),
    PTG("Open Mission Control Dataset", "*.ptg"),
    ARCHIV("Archive", "*.zip", "*.tar.gz", "*.tgz", "*.rar"),
    WKT_DC("dc", "*.dc"),
    WKT_PRJ("prj", "*.prj"),
    JPEG("JPEG", "*.jpg", "*.jpeg"),
    XMP("XMP", "*.xmp"),
    JPEG_ASC_TEC("AscTec JPEG", "*.jpg"),
    FALCON8_EXPORT("AscTecNavigator.exe"),
    FALCON8PLUS_EXPORT("AscTecNavigator.exe"),
    FLIGHTPLAN("Mission Control missions", "*.fml"),
    ANP("Intel® Falcon 8+ project", "*.anp"),
    ACP("Intel® Falcon 8+ teach-in project", "*.acp"),
    AGISOFT_PHOTOSCAN_APPLICATION("Agisoft Photo Scan", "*.exe"),
    AGISOFT_METASHAPE_APPLICATION("Agisoft Metashape", "*.exe"),
    PIX4D_APPLICATION("Pix4D", "*.exe"),
    MENCI_APS_APPLICATION("Menci APS", "*.exe"),
    CONTEXTCAPTURE_APPLICATION("Context Capture", "*.exe"),
    THREED_SURVEY_APPLICATION("3D Survey", "*.exe"),
    CSV("CSV", "*.CSV"),
    OBJ("OBJ", "*.obj"),
    RAW("RAW", "*.tif", "*.tiff"),
    JPEG_RAW_XMP("JPEG, RAW, XMP", "*.jpg", "*.jpeg", "*.tif", "*.tiff", "*.xmp")
    ;

    private final String name;
    private final String[] extensions;

    FileFilter(String extension) {
        this(extension, extension);
    }

    FileFilter(String[] extensions) {
        this(String.join(", ", extensions), extensions);
    }

    FileFilter(String name, String... extensions) {
        this.name = name;
        this.extensions = extensions;
    }

    public String getName() {
        return name;
    }

    public String[] getExtensions() {
        return extensions;
    }
}
