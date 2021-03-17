/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.intel.missioncontrol.INotificationObject;

import java.util.List;

public interface IPayloadMountConfiguration extends INotificationObject {

    String DESCRIPTION_PROPERTY = "description";
    String PAYLOADS_PROPERTY = "payloads";

    IPayloadMountDescription getDescription();

    void setDescription(IPayloadMountDescription description);

    void addPayload(IPayloadConfiguration payload);

    void removePayload(IPayloadConfiguration payload);

    int getPayloadCount();

    /** Returns a read-only view of the specified payloads. */
    <T extends IPayloadConfiguration> List<T> getPayloads(Class<T> payloadClass);

    /** Returns a read-only view of all payloads in this payload mount. */
    List<IPayloadConfiguration> getPayloads();

    <T extends IPayloadConfiguration> T getPayload(Class<T> payloadClass, int payloadIndex);

    IPayloadConfiguration getPayload(int payloadIndex);

    void setPayload(int payloadIndex, IPayloadConfiguration payload);

    /**
     * Returns the first payload in this payload mount as a typed instance.
     *
     * @throws HardwareConfigurationException The first payload of this payload mount is not of the specified type, or
     *     no payloads are available.
     */
    <T extends IPayloadConfiguration> T getFirstPayload(Class<T> payloadClass);

    /**
     * Returns the first payload in this payload mount as an untyped instance.
     *
     * @throws HardwareConfigurationException No payloads are available.
     */
    IPayloadConfiguration getFirstPayload();

    void setFirstPayload(IPayloadConfiguration payload);

    IPayloadMountConfiguration deepCopy();

}
