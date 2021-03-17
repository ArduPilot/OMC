/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.helper.Expect;
import java.util.ArrayList;
import java.util.List;

class HardwareConfiguration extends NotificationObject implements IHardwareConfiguration {

    private transient boolean immutable = false;
    private IPlatformDescription platformDescription;
    private final List<IPayloadMountConfiguration> payloadMounts = new ArrayList<>();

    static HardwareConfiguration createImmutable(IPlatformDescription desc, IGenericCameraConfiguration camera) {
        HardwareConfiguration config = new HardwareConfiguration(desc, camera);
        config.immutable = true;
        return config;
    }

    /**
     * Initializes a new instance of the HardwareConfiguration class with a primary payload.
     *
     * @throws HardwareConfigurationException The platform has no payload mounts.
     * @throws IllegalArgumentException Any parameter is null.
     */
    HardwareConfiguration(IPlatformDescription platformDescription, IPayloadConfiguration payload) {
        this(platformDescription);
        Expect.notNull(payload, "payload");
        payloadMounts.get(0).addPayload(payload);
    }

    private HardwareConfiguration(IPlatformDescription platformDescription) {
        Expect.notNull(platformDescription, "platformDescription");

        List<IPayloadMountDescription> payloadMountDescriptions = platformDescription.getPayloadMountDescriptions();
        if (payloadMountDescriptions.isEmpty()) {
            throw new HardwareConfigurationException("Platform has no payload mounts.");
        }

        registerSubObject(platformDescription);
        this.platformDescription = platformDescription;

        for (IPayloadMountDescription desc : payloadMountDescriptions) {
            IPayloadMountConfiguration payloadMount = new PayloadMountConfiguration(desc);
            registerSubObject(payloadMount);
            this.payloadMounts.add(payloadMount);
        }
    }

    @Override
    public IPlatformDescription getPlatformDescription() {
        return platformDescription;
    }

    @Override
    public void setPlatformDescription(IPlatformDescription description) {
        Expect.notNull(description, "description");
        verifyMutable();
        if (!this.platformDescription.equals(description)) {
            IPlatformDescription oldValue = this.platformDescription;
            this.platformDescription = description;
            unregisterSubObject(oldValue);
            registerSubObject(description);
            notifyPropertyChanged(PLATFORM_DESCRIPTION_PROPERTY, oldValue, description);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IPayloadConfiguration> T getPayload(Class<T> payloadClass) {
        for (IPayloadMountConfiguration payloadMount : payloadMounts) {
            for (IPayloadConfiguration payload : payloadMount.getPayloads()) {
                if (payloadClass.isAssignableFrom(payload.getClass())) {
                    return (T)payload;
                }
            }
        }

        throw new HardwareConfigurationException("No payload of type " + payloadClass.getName() + " available.");
    }

    @Override
    public <T extends IPayloadConfiguration> T getPayload(Class<T> payloadClass, int mountIndex, int payloadIndex) {
        if (mountIndex < 0 || mountIndex >= payloadMounts.size()) {
            throw new HardwareConfigurationException("The requested payload mount does not exist.");
        }

        IPayloadMountConfiguration payloadMount = payloadMounts.get(mountIndex);
        return payloadMount.getPayload(payloadClass, payloadIndex);
    }

    @Override
    public IPayloadConfiguration getPayload(int mountIndex, int payloadIndex) {
        if (mountIndex < 0 || mountIndex >= payloadMounts.size()) {
            throw new HardwareConfigurationException("The requested payload mount does not exist.");
        }

        IPayloadMountConfiguration payloadMount = payloadMounts.get(mountIndex);
        return payloadMount.getPayload(payloadIndex);
    }

    @Override
    public <T extends IPayloadConfiguration> boolean hasPrimaryPayload(Class<T> payloadClass) {
        if (payloadMounts.size() == 0) {
            return false;
        }

        IPayloadMountConfiguration payloadMount = payloadMounts.get(0);
        if (payloadMount == null || payloadMount.getPayloadCount() == 0) {
            return false;
        }

        IPayloadConfiguration payload = payloadMount.getFirstPayload();
        boolean assignable = payloadClass.isAssignableFrom(payload.getClass());
        return assignable;
    }

    @Override
    public <T extends IPayloadConfiguration> T getPrimaryPayload(Class<T> payloadClass) {
        return payloadMounts.get(0).getFirstPayload(payloadClass);
    }

    @Override
    public IPayloadConfiguration getPrimaryPayload() {
        return payloadMounts.get(0).getFirstPayload();
    }

    @Override
    public void setPrimaryPayload(IPayloadConfiguration payload) {
        Expect.notNull(payload, "payload");
        verifyMutable();
        IPayloadConfiguration oldValue = getPrimaryPayload();
        if (!oldValue.equals(payload)) {
            this.payloadMounts.get(0).setFirstPayload(payload);
            unregisterSubObject(oldValue);
            registerSubObject(payload);
            notifyPropertyChanged(PAYLOADS_PROPERTY, oldValue, payload);
        }
    }

    @Override
    public IPayloadMountConfiguration[] getPayloadMounts() {
        return payloadMounts.toArray(new IPayloadMountConfiguration[0]);
    }

    @Override
    public IPayloadMountConfiguration getPayloadMount(int mountIndex) {
        IPayloadMountConfiguration mount = payloadMounts.get(mountIndex);
        if (mount == null) {
            throw new HardwareConfigurationException("The requested payload mount does not exist.");
        }

        return mount;
    }

    @Override
    public void initializeFrom(IHardwareConfiguration hardwareConfiguration) {
        if (this.equals(hardwareConfiguration)) {
            return;
        }

        platformDescription = hardwareConfiguration.getPlatformDescription();

        IPayloadMountConfiguration[] otherPayloadMounts = hardwareConfiguration.getPayloadMounts();
        if (otherPayloadMounts.length == 0) {
            throw new IllegalArgumentException("No payload mounts available.");
        }

        for (IPayloadMountConfiguration payloadMount : payloadMounts) {
            unregisterSubObject(payloadMount);
        }

        payloadMounts.clear();
        for (IPayloadMountConfiguration payloadMount : otherPayloadMounts) {
            registerSubObject(payloadMount);
            payloadMounts.add(payloadMount);
        }

        if (hardwareConfiguration instanceof HardwareConfiguration) {
            immutable = ((HardwareConfiguration)hardwareConfiguration).immutable;
        }

        notifyPropertyChanged("", null, null);
    }

    @Override
    public void setConfigurationFrom(IHardwareConfiguration other) {
        IGenericCameraConfiguration payload = other.getPrimaryPayload(IGenericCameraConfiguration.class);
        setPlatformDescription(other.getPlatformDescription());
        getPrimaryPayload(IGenericCameraConfiguration.class).setDescription(payload.getDescription());
        getPrimaryPayload(IGenericCameraConfiguration.class)
            .getLens()
            .setDescription(payload.getLens().getDescription());
    }

    @Override
    public IHardwareConfiguration deepCopy() {
        HardwareConfiguration config = new HardwareConfiguration(platformDescription);
        int lowerBound = Math.min(payloadMounts.size(), config.payloadMounts.size());
        int upperBound = Math.max(payloadMounts.size(), config.payloadMounts.size());

        for (int i = 0; i < lowerBound; ++i) {
            IPayloadMountConfiguration payloadMountCopy = payloadMounts.get(i).deepCopy();
            config.registerSubObject(payloadMountCopy);
            config.payloadMounts.set(i, payloadMountCopy);
        }

        if (lowerBound != upperBound && config.payloadMounts.size() == upperBound) {
            for (int i = lowerBound; i < upperBound; ++i) {
                config.payloadMounts.set(i, null);
            }
        }

        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof HardwareConfiguration)) {
            return false;
        }

        HardwareConfiguration other = (HardwareConfiguration)obj;
        return platformDescription.equals(other.platformDescription)
            && immutable == other.immutable
            && payloadMounts.equals(other.payloadMounts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(platformDescription, immutable, payloadMounts);
    }

    @Override
    public String toString() {
        return platformDescription.getId();
    }

    private void verifyMutable() {
        if (immutable) {
            throw new IllegalStateException("The configuration is immutable and cannot be changed.");
        }
    }

}
