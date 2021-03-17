/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.support;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.ui.dialogs.MatchingsTableRowData;
import eu.mavinci.core.helper.MProperties;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import java.io.File;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * This interface defines the basic back-end operations available in the "Support Request" dialog: upload support files
 * ( with possibility to cancel in the process), upload later and upload old support requests.
 *
 * @author aiacovici
 */
public interface ISupportManager {

    /**
     * This method will copy the default files, as well as the files selected by the user from their original location
     * to the folder MAVinci_Desktop_INSTALLATION_PATH\errorReports\TIMESTAMP folder and archived. This method can be
     * called both when uploading files right now, as well as when uploading later.
     *
     * <p>The default files are the following: Application log: one file:
     * MAVinci_Desktop_INSTALLATION_PATH\appLogs\MAVinciDesktop_0_0.log Old application logs : all other log files found
     * under the folder: MAVinci_Desktop_INSTALLATION_PATH\appLogs Application settings : one file: Intel Mission
     * Control_INSTALLATION_PATH\appSettings.xml Camera settings: all files under the folder: Intel Mission
     * Control_INSTALLATION_PATH\lenses. These files have a ".camera" extension. meta.xml - XML file containing a list
     * of all the uploaded files ( including itself) with a full path on the disk, the message wrote by the user, date,
     * serial number of the application plus some other parameters.
     *
     * <p>Depending on selected checkbox in the "Include" section, archives of each of the following files are being
     * uploaded: Session settings: one file: ** Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\settings.json Flight plans: folder and included files ** Intel
     * Mission Control_INSTALLATION_PATH\sessions\SESSION_NAME\flightplans FTP Folder: folder and all included files:
     * Intel Mission Control_INSTALLATION_PATH\sessions\SESSION_NAME\ftp UAV Logs: folder and all included files: Intel
     * Mission Control_INSTALLATION_PATH\sessions\SESSION_NAME\log KML: folder and all included files: Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\kml UAV Config: folder and all included files: Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\planeconfig Images: images taken during the current session, in
     * full resolution or preview version. They are taken from the folder: Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\images
     *
     * @param category - the category of the error, of enum type ErrorCategory
     * @param priority - the priority of the error, of enum type Priority
     * @param problemDescription - the message written by the user in the "Problem description" box
     * @param options - a map representing the options selected by the user. The key contains a string constant
     *     representing one of the options, and the value is a boolean with value true, when the option was selected and
     *     false when not.
     * @param matchings - the matching files selected by the user
     * @param additionals - optional list of additional files to be sent to support for analysis
     * @return File representing the time-stamp folder where the files were copied
     * @see SupportConstants
     */
    File prepareFilesForTransfer(
            ErrorCategory category,
            Priority priority,
            String problemDescription,
            Map<String, Boolean> options,
            List<MatchingsTableRowData> matchings,
            List<File> additionals,
            List<String> recipient,
            String fullName,
            String country,
            String ticketIdOld);

    /**
     * This method will copy the default files, as well as the files selected by the user from their original location
     * to the folder MAVinci_Desktop_INSTALLATION_PATH\errorReports\TIMESTAMP folder and archived. Each file / folder is
     * zipped separately. A ticket id is generated from a hashcode and a timestamp. Example:
     * 885d731495001dc63480f075276db9ab_1494406786422 The archived files will then be uploaded one by one via SFTP on
     * mavinci.de server in a folder /data/<TICKET_ID>
     *
     * <p>The default files are the following: Application log: one file:
     * MAVinci_Desktop_INSTALLATION_PATH\appLogs\MAVinciDesktop_0_0.log Old application logs : all other log files found
     * under the folder: MAVinci_Desktop_INSTALLATION_PATH\appLogs Application settings : one file: Intel Mission
     * Control_INSTALLATION_PATH\appSettings.xml Camera settings: all files under the folder: Intel Mission
     * Control_INSTALLATION_PATH\lenses. These files have a ".camera" extension. meta.xml - XML file containing a list
     * of all the uploaded files ( including itself) with a full path on the disk, the message wrote by the user, date,
     * serial number of the application plus some other parameters.
     *
     * <p>Depending on selected checkbox in the "Include" section, archives of each of the following files are being
     * uploaded: Session settings: one file: ** Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\settings.json Flight plans: folder and included files ** Intel
     * Mission Control_INSTALLATION_PATH\sessions\SESSION_NAME\flightplans FTP Folder: folder and all included files:
     * Intel Mission Control_INSTALLATION_PATH\sessions\SESSION_NAME\ftp UAV Logs: folder and all included files: Intel
     * Mission Control_INSTALLATION_PATH\sessions\SESSION_NAME\log KML: folder and all included files: Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\kml UAV Config: folder and all included files: Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\planeconfig Images: images taken during the current session, in
     * full resolution or preview version. They are taken from the folder: Intel Mission
     * Control_INSTALLATION_PATH\sessions\SESSION_NAME\images
     *
     * @param category - the category of the error, of enum type ErrorCategory
     * @param priority - the priority of the error, of enum type Priority
     * @param problemDescription - the message written by the user in the "Problem description" box
     * @param options - a map representing the options selected by the user. The key contains a string constant
     *     representing one of the options, and the value is a boolean with value true, when the option was selected and
     *     false when not.
     * @param matchings - - the matching files selected by the user
     * @param additionals - optional list of additional files to be sent to support for analysis
     * @see SupportConstants
     */
    void sendFilesToServer(
            ErrorCategory category,
            Priority priority,
            String problemDescription,
            Map<String, Boolean> options,
            List<MatchingsTableRowData> matchings,
            List<File> additionals,
            List<String> recipients,
            IMProgressMonitor monitor,
            String fullName,
            String country,
            String ticketIdOld);

    List<File> getFilesForRequest(Map<String, Boolean> options, List<MatchingsTableRowData> matchingsData);

    void doUpload(final File errorReportFolder, IMProgressMonitor monitor);

    void doDownload(String ticketId, IMProgressMonitor monitor, IApplicationContext applicationContext);

    MProperties getReportProperties(File errorReportFolder);

    ReadOnlyBooleanProperty hasOldSupportRequestsProperty();

    void scanReportFolder();

    void checkErrorReports();
}
