/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.settings.Serializable;
import org.apache.commons.lang3.NotImplementedException;

@Serializable
public class MavlinkCameraConnectionItem implements IMavlinkConnectionItem {
    private final AsyncBooleanProperty isOnline = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty isKnown = new SimpleAsyncBooleanProperty(this);
    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncIntegerProperty cameraNumber = new SimpleAsyncIntegerProperty(this);
    private final AsyncStringProperty descriptionId = new SimpleAsyncStringProperty(this);
    private final AsyncObjectProperty<TcpIpTransportType> transportType = new SimpleAsyncObjectProperty<>(this);
    private final AsyncStringProperty host = new SimpleAsyncStringProperty(this);
    private final AsyncIntegerProperty port = new SimpleAsyncIntegerProperty(this);
    private final AsyncIntegerProperty systemId = new SimpleAsyncIntegerProperty(this);
    private final AsyncIntegerProperty componentId = new SimpleAsyncIntegerProperty(this);

    MavlinkCameraConnectionItem(
            String name,
            int cameraNumber,
            String cameraDescriptionId,
            boolean isOnline,
            boolean isKnown,
            TcpIpTransportType transportType,
            String host,
            int port,
            int systemId,
            int componentId) {
        this.name.set(name);
        this.cameraNumber.set(cameraNumber);
        this.descriptionId.set(cameraDescriptionId);
        this.isOnline.set(isOnline);
        this.isKnown.set(isKnown);
        this.transportType.set(transportType);
        this.host.set(host);
        this.port.set(port);
        this.systemId.set(systemId);
        this.componentId.set(componentId);
    }

    private MavlinkCameraConnectionItem(MavlinkCameraConnectionItem other) {
        set(other);
    }

    public void set(MavlinkCameraConnectionItem other) {
        this.isOnline.set(other.isOnline.get());
        this.isKnown.set(other.isKnown.get());
        this.cameraNumber.set(other.cameraNumber.get());
        this.name.set(other.name.get());
        this.descriptionId.set(other.descriptionId.get());
        this.transportType.set(other.transportType.get());
        this.host.set(other.host.get());
        this.port.set(other.port.get());
        this.systemId.set(other.getSystemId());
        this.componentId.set(other.getComponentId());
    }

    @Override
    public void set(IConnectionItem other) {
        if (other instanceof MavlinkCameraConnectionItem) {
            set((MavlinkCameraConnectionItem)other);
        } else {
            throw new IllegalArgumentException("Invalid IConnectionItem type");
        }
    }

    @Override
    public void bindContent(IConnectionItem other) {
        throw new NotImplementedException("bindContent not implemented");
    }

    @Override
    public void unbind() {
        throw new NotImplementedException("unbind not implemented");
    }

    @Override
    public boolean isBound() {
        return false;
    }

    @Override
    public String toString() {
        return "MavlinkCameraConnectionItem{"
            + getName()
            + ", "
            + getDescriptionId()
            + ", "
            + getTransportType()
            + ", host="
            + getHost()
            + ", port="
            + getPort()
            + ", sysId="
            + getSystemId()
            + '}';
    }

    @Override
    public boolean isSameConnection(IReadOnlyConnectionItem other) {
        if (!(other instanceof MavlinkCameraConnectionItem)) return false;

        MavlinkCameraConnectionItem o = (MavlinkCameraConnectionItem)other;

        return getHost().equals(o.getHost())
            && getPort() == o.getPort()
            && getTransportType() == o.getTransportType()
            && getCameraNumber() == o.getCameraNumber()
            && getSystemId() == o.getSystemId();
    }

    public AsyncStringProperty nameProperty() {
        return name;
    }

    @Override
    public AsyncStringProperty descriptionIdProperty() {
        return descriptionId;
    }

    @Override
    public AsyncBooleanProperty isOnlineProperty() {
        return isOnline;
    }

    @Override
    public AsyncBooleanProperty isKnownProperty() {
        return isKnown;
    }

    @Override
    public IConnectionItem createMutableCopy() {
        return new MavlinkCameraConnectionItem(this);
    }

    public AsyncIntegerProperty cameraNumberProperty() {
        return cameraNumber;
    }

    @Override
    public AsyncObjectProperty<TcpIpTransportType> transportTypeProperty() {
        return transportType;
    }

    @Override
    public AsyncStringProperty hostProperty() {
        return host;
    }

    @Override
    public AsyncIntegerProperty portProperty() {
        return port;
    }

    public int getCameraNumber() {
        return cameraNumber.get();
    }

    @Override
    public AsyncIntegerProperty systemIdProperty() {
        return systemId;
    }

    public AsyncIntegerProperty componentIdProperty() {
        return componentId;
    }

    public int getComponentId() {
        return componentId.get();
    }
}
