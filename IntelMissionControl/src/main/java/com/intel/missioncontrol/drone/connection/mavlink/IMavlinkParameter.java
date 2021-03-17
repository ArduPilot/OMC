/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

public interface IMavlinkParameter {
    /** Get this parameter's Id (String with up to 16 characters). */
    String getId();

    /**
     * Get this parameter's value as an int. Only use if the parameter Id is known to be convertible to int. Throws an
     * InvalidParamTypeException if not convertible.
     */
    int getIntValue();

    /**
     * Get this parameter's value as a long. Only use if the parameter Id is known to be convertible to long. Throws an
     * InvalidParamTypeException if not convertible.
     */
    long getLongValue();

    /**
     * Get this parameter's value as a float. Only use if the parameter Id is known to be convertible to float. Throws
     * an InvalidParamTypeException if not convertible.
     */
    float getFloatValue();

    /**
     * Get this parameter's value as a double. Only use if the parameter Id is known to be convertible to double. Throws
     * an InvalidParamTypeException if not convertible.
     */
    double getDoubleValue();

    /**
     * Get this parameter's value as a String. Only use if the parameter Id is known to be convertible to String (true
     * for some extended parameters). Throws an InvalidParamTypeException if not convertible.
     */
    String getStringValue();

}
