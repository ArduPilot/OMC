/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.support;

import eu.mavinci.core.obfuscation.IKeepClassname;
import eu.mavinci.desktop.helper.uploader.Uploader;

import java.text.DecimalFormat;

/**
 * This class define constant values, needed in the "Help and Support" API.
 *
 * @author aiacovici
 */
public class SupportConstants implements IKeepClassname {

    /** These constants represent the options selected or not by the user in the "Support Request" dialog. */
    public static final String OPTION_SESSION_SETTINGS = "SESSION_SETTINGS";

    public static final String OPTION_APPLICATION_SETTINGS = "APPLICATION_SETTINGS";
    public static final String OPTION_SCREENSHOTS = "SCREENSHOTS";

    /** constants for name of the files that will be by default sent to support. */
    public static final String FILENAME_META = "meta.xml";

    public static final String FILENAME_INSTALL_LOG = "install.log";

    public static final String INSTALL_LOG_FILE = Uploader.INSTALL_LOG_FILE;

    /** the following are constans representing keys for the MProperties map. */
    public static final String KEY_STORED_COMMENT = "__description";

    public static final String KEY_STORED_ERR_COUNT = "__errCount";
    public static final String KEY_STORED_AUTOMATIC = "__automatic";
    public static final String KEY_PRIORITY = "__priority";
    public static final String KEY_UPTIME = "__uptimeMSec";
    public static final String KEY_DATE = "__date";
    public static final String KEY_SIZE = "__size";
    public static final String KEY_RECIPIENTS = "__recipients";
    public static final String KEY_FULLNAME = "__fullName";
    public static final String KEY_COUNTRY = "__country";
    public static final String KEY_CATEGORY = "__category";
    public static final String KEY_TICKETIDOLD = "__ticketIdOld";

    public static final String KEY_SUFFIX_RESEND = ".resend";
    public static final String KEY_SUFFIX_BASEPATH = ".basePath";

    public static final DecimalFormat NUMBER_PREFIX_FORMAT = new DecimalFormat("000000");

    public static final int DEFAULT_RETRIES = 4;
    public static final int RETRY_WAIT_SEC = 20;

}
