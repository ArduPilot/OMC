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

@Serializable
public class MavlinkDroneConnectionItem implements IMavlinkConnectionItem {
    private final transient AsyncBooleanProperty isOnline = new SimpleAsyncBooleanProperty(this);
    private final transient AsyncBooleanProperty isKnown = new SimpleAsyncBooleanProperty(this);
    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty platformId = new SimpleAsyncStringProperty(this);
    private final AsyncObjectProperty<TcpIpTransportType> transportType = new SimpleAsyncObjectProperty<>(this);
    private final AsyncStringProperty host = new SimpleAsyncStringProperty(this);
    private final AsyncIntegerProperty port = new SimpleAsyncIntegerProperty(this);
    private final AsyncIntegerProperty systemId = new SimpleAsyncIntegerProperty(this);

    private transient boolean isBound;

    @SuppressWarnings("unused")
    private MavlinkDroneConnectionItem() {}

    public void initialize() {
        //TODO initialize default property values
        isBound = false;
        isOnlineProperty().set(false);
        isKnownProperty().set(true);
    }

    public MavlinkDroneConnectionItem(
            boolean isOnline,
            boolean isKnown,
            String name,
            String platformId,
            TcpIpTransportType transportType,
            String host,
            int port,
            int systemId) {
        initialize();

        this.isOnline.set(isOnline);
        this.isKnown.set(isKnown);
        this.name.set(name);
        this.platformId.set(platformId);
        this.transportType.set(transportType);
        this.host.set(host);
        this.port.set(port);
        this.systemId.set(systemId);
    }

    public MavlinkDroneConnectionItem(MavlinkDroneConnectionItem other) {
        initialize();
        set(other);
    }

    @Override
    public String toString() {
        return "MavlinkDroneConnectionItem{"
            + getName()
            + ", "
            + getPlatformId()
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
        if (!(other instanceof MavlinkDroneConnectionItem)) return false;

        MavlinkDroneConnectionItem o = (MavlinkDroneConnectionItem)other;

        return getHost().equals(o.getHost())
            && getPort() == o.getPort()
            && getTransportType() == o.getTransportType()
            && getSystemId() == ((MavlinkDroneConnectionItem)other).getSystemId();
    }

    public void set(MavlinkDroneConnectionItem other) {
        this.isOnline.set(other.isOnline());
        this.isKnown.set(other.isKnown());
        this.name.set(other.getName());
        this.platformId.set(other.getPlatformId());
        this.transportType.set(other.getTransportType());
        this.host.set(other.getHost());
        this.port.set(other.getPort());
        this.systemId.set(other.getSystemId());
    }

    public boolean isBound() {
        return isBound;
    }

    @Override
    public void bindContent(IConnectionItem other) {
        if (!(other instanceof MavlinkDroneConnectionItem)) {
            throw new IllegalArgumentException("Other item must be of same type");
        }

        var o = (MavlinkDroneConnectionItem)other;

        this.name.bind(o.name);
        this.platformId.bind(o.platformId);
        this.transportType.bind(o.transportType);
        this.host.bind(o.host);
        this.port.bind(o.port);
        this.systemId.bind(o.systemId);
        isBound = true;
    }

    @Override
    public void unbind() {
        if (isBound) {
            this.name.unbind();
            this.platformId.unbind();
            this.transportType.unbind();
            this.host.unbind();
            this.port.unbind();
            this.systemId.unbind();
            isBound = false;
        }
    }

    public AsyncStringProperty nameProperty() {
        return name;
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
    public void set(IConnectionItem other) {
        if (other instanceof MavlinkDroneConnectionItem) {
            set((MavlinkDroneConnectionItem)other);
        } else {
            throw new IllegalArgumentException("Invalid IConnectionItem type");
        }
    }

    @Override
    public IConnectionItem createMutableCopy() {
        return new MavlinkDroneConnectionItem(this);
    }

    @Override
    public AsyncStringProperty descriptionIdProperty() {
        return platformId;
    }

    public AsyncStringProperty platformIdProperty() {
        return platformId;
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

    public String getPlatformId() {
        return platformId.get();
    }

    public String getHost() {
        return host.get();
    }

    public int getPort() {
        return port.get();
    }

    @Override
    public AsyncIntegerProperty systemIdProperty() {
        return systemId;
    }
}
