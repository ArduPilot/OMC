/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;

public interface IHardwareConfiguration extends INotificationObject {

    String PLATFORM_DESCRIPTION_PROPERTY = "platformDescription";
    String PAYLOADS_PROPERTY = "payloads";

    IPlatformDescription getPlatformDescription();

    void setPlatformDescription(IPlatformDescription description);

    /**
     * Returns the first payload of the specified type.
     *
     * @throws HardwareConfigurationException No payload of the specified type was found.
     */
    <T extends IPayloadConfiguration> T getPayload(Class<T> payloadClass);

    /**
     * Returns the specified payload as a typed instance.
     *
     * @throws HardwareConfigurationException The specified payload does not exist or is of a different type.
     */
    <T extends IPayloadConfiguration> T getPayload(Class<T> payloadClass, int mountIndex, int payloadIndex);

    /**
     * Returns the specified payload as an untyped instance.
     *
     * @throws HardwareConfigurationException The specified payload does not exist.
     */
    IPayloadConfiguration getPayload(int mountIndex, int payloadIndex);

    /** Returns whether a primary payload of the specified type exists. */
    <T extends IPayloadConfiguration> boolean hasPrimaryPayload(Class<T> payloadClass);

    /**
     * Returns the primary payload as a typed instance.
     *
     * @throws HardwareConfigurationException The primary payload is not of the specified type, or no primary payload
     *     was found.
     */
    <T extends IPayloadConfiguration> T getPrimaryPayload(Class<T> payloadClass);

    /**
     * Returns the primary payload as an untyped instance.
     *
     * @throws HardwareConfigurationException No primary payload was found.
     */
    IPayloadConfiguration getPrimaryPayload();

    void setPrimaryPayload(IPayloadConfiguration payload);

    IPayloadMountConfiguration[] getPayloadMounts();

    IPayloadMountConfiguration getPayloadMount(int mountIndex);

    IHardwareConfiguration deepCopy();

    void initializeFrom(IHardwareConfiguration configuration);

    void setConfigurationFrom(IHardwareConfiguration other);

}
