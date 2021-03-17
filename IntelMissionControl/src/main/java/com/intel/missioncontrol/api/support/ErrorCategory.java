/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api.support;

import eu.mavinci.core.obfuscation.IKeepAll;

/**
 * This enumeration defines the few types of error categories available in the drop down list in the "Support Request"
 * dialog. For the dropdown list, customer's estimation type of case:
 *
 * <ul>
 *   <li>General
 *   <li>Mission planning, workflows
 *   <li>Post processing data
 *   <li>Licenses, Updating Software and Firmware
 *   <li>Emergency and accidents
 *   <li>Maintenance and repair
 * </ul>
 *
 * @author aiacovici
 */
public enum ErrorCategory implements IKeepAll {
    SESSION_ISSUE,
    CLOSED_UNEXPECTEDLY,
    BEHAVED_UNPREDICTABLY,
    POST_PROCESSING_ISSUE,
    LICENSE_ISSUE,
    OTHER_ISSUES
}
