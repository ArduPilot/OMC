/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.hardware;

import com.google.common.base.Objects;
import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.NotificationObject;
import com.intel.missioncontrol.helper.Expect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class PayloadMountConfiguration extends NotificationObject implements IPayloadMountConfiguration {

    private static int MAX_PAYLOADS = 16;

    private static class DummyPayload implements IPayloadConfiguration {
        @Override
        public IPayloadDescription getDescription() {
            throw new UnsupportedOperationException("Invalid access of unconfigured payload.");
        }

        @Override
        public IPayloadConfiguration deepCopy() {
            throw new UnsupportedOperationException("Invalid access of unconfigured payload.");
        }

        @Override
        public void addListener(INotificationObject.ChangeListener listener) {
            throw new UnsupportedOperationException("Invalid access of unconfigured payload.");
        }

        @Override
        public void removeListener(INotificationObject.ChangeListener listener) {
            throw new UnsupportedOperationException("Invalid access of unconfigured payload.");
        }
    }

    private IPayloadMountDescription description;
    private final List<IPayloadConfiguration> payloads = new ArrayList<>();

    public PayloadMountConfiguration(
            IPayloadMountDescription payloadMountDescription, IPayloadConfiguration... payloads) {
        this.description = payloadMountDescription;
        registerSubObject(payloadMountDescription);

        if (payloads.length > 0) {
            this.payloads.addAll(Arrays.asList(payloads));
            for (IPayloadConfiguration payload : this.payloads) {
                registerSubObject(payload);
            }
        }
    }

    @Override
    public IPayloadMountDescription getDescription() {
        return description;
    }

    @Override
    public void setDescription(IPayloadMountDescription description) {
        Expect.notNull(description, "description");
        if (this.description != description) {
            IPayloadMountDescription oldValue = this.description;
            unregisterSubObject(oldValue);
            registerSubObject(description);
            this.description = description;
            notifyPropertyChanged(DESCRIPTION_PROPERTY, oldValue, description);
        }
    }

    @Override
    public void addPayload(IPayloadConfiguration payload) {
        Expect.notNull(payload, "payload");
        if (payloads.add(payload)) {
            registerSubObject(payload);
            notifyPropertyChanged(PAYLOADS_PROPERTY, null, payload);
        }
    }

    @Override
    public void removePayload(IPayloadConfiguration payload) {
        Expect.notNull(payload, "payload");
        if (payloads.remove(payload)) {
            unregisterSubObject(payload);
            notifyPropertyChanged(PAYLOADS_PROPERTY, payload, null);
        }
    }

    @Override
    public int getPayloadCount() {
        return payloads.size();
    }

    @Override
    public <T extends IPayloadConfiguration> List<T> getPayloads(Class<T> payloadClass) {
        return Collections.unmodifiableList(
            payloads.stream()
                .filter(p -> payloadClass.isAssignableFrom(p.getClass()))
                .map(payloadClass::cast)
                .collect(Collectors.toList()));
    }

    @Override
    public List<IPayloadConfiguration> getPayloads() {
        return Collections.unmodifiableList(payloads);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IPayloadConfiguration> T getPayload(Class<T> payloadClass, int payloadIndex) {
        if (payloadIndex < 0 || payloadIndex >= payloads.size()) {
            throw new HardwareConfigurationException("The requested payload does not exist.");
        }

        IPayloadConfiguration payload = payloads.get(payloadIndex);
        if (payload instanceof DummyPayload) {
            throw new HardwareConfigurationException("The requested payload was not configured.");
        }

        if (!(payloadClass.isAssignableFrom(payload.getClass()))) {
            throw new HardwareConfigurationException(
                "The requested payload has a different type (requested: "
                    + payloadClass.getName()
                    + ", actual: "
                    + payload.getClass().getName()
                    + ").");
        }

        return (T)payload;
    }

    @Override
    public IPayloadConfiguration getPayload(int payloadIndex) {
        if (payloadIndex < 0 || payloadIndex >= payloads.size()) {
            throw new HardwareConfigurationException("The requested payload does not exist.");
        }

        IPayloadConfiguration payload = payloads.get(payloadIndex);
        if (payload instanceof DummyPayload) {
            throw new HardwareConfigurationException("The requested payload was not configured.");
        }

        return payload;
    }

    @Override
    public void setPayload(int payloadIndex, IPayloadConfiguration payload) {
        Expect.notNull(payload, "payload");
        Expect.isTrue(payloadIndex < MAX_PAYLOADS, "payloadIndex");

        while (payloads.size() <= payloadIndex) {
            payloads.add(new DummyPayload());
        }

        if (!payload.equals(payloads.get(payloadIndex))) {
            payloads.set(payloadIndex, payload);
            notifyPropertyChanged(PAYLOADS_PROPERTY, payloads, payloads);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IPayloadConfiguration> T getFirstPayload(Class<T> payloadClass) {
        if (payloads.isEmpty()) {
            throw new HardwareConfigurationException("No payloads available.");
        }

        IPayloadConfiguration payload = payloads.get(0);
        if (!(payloadClass.isAssignableFrom(payload.getClass()))) {
            throw new HardwareConfigurationException(
                "The first payload is not of type " + payloadClass.getName() + ".");
        }

        return (T)payload;
    }

    @Override
    public IPayloadConfiguration getFirstPayload() {
        if (payloads.isEmpty()) {
            throw new HardwareConfigurationException("No payloads available.");
        }

        return payloads.get(0);
    }

    @Override
    public void setFirstPayload(IPayloadConfiguration payload) {
        Expect.notNull(payload, "payload");
        if (payloads.isEmpty()) {
            registerSubObject(payload);
            payloads.add(payload);
            notifyPropertyChanged(PAYLOADS_PROPERTY, null, payload);
        } else {
            IPayloadConfiguration oldValue = payloads.get(0);
            if (!oldValue.equals(payload)) {
                unregisterSubObject(oldValue);
                registerSubObject(payload);
                payloads.set(0, payload);
                notifyPropertyChanged(PAYLOADS_PROPERTY, oldValue, payload);
            }
        }
    }

    @Override
    public IPayloadMountConfiguration deepCopy() {
        PayloadMountConfiguration config = new PayloadMountConfiguration(description);
        for (IPayloadConfiguration payload : payloads) {
            IPayloadConfiguration payloadCopy = payload.deepCopy();
            config.registerSubObject(payloadCopy);
            config.payloads.add(payloadCopy);
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

        if (!(obj instanceof PayloadMountConfiguration)) {
            return false;
        }

        PayloadMountConfiguration other = (PayloadMountConfiguration)obj;
        return description.equals(other.description) && payloads.equals(other.payloads);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(description, payloads);
    }

}
