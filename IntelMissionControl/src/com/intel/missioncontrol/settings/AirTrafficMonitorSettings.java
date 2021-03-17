/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.AsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncIntegerProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;

@SettingsMetadata(section = "airTrafficMonitor")
public class AirTrafficMonitorSettings implements ISettings {

    private final AsyncIntegerProperty comPort =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(0).create());

    private final AsyncIntegerProperty baudRate =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(115_200).create());

    private final AsyncIntegerProperty dataBits =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(8).create());

    private final AsyncIntegerProperty stopBits =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(1).create());

    private final AsyncObjectProperty<Parity> parity =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Parity>().initialValue(Parity.NONE).create());

    private final AsyncDoubleProperty minimumHorizontalDistance =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(0).create());

    private final AsyncDoubleProperty minimumVerticalDistance =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(0).create());

    public AsyncIntegerProperty comPortProperty() {
        return this.comPort;
    }

    public AsyncIntegerProperty baudRateProperty() {
        return this.baudRate;
    }

    public AsyncIntegerProperty dataBitsProperty() {
        return this.dataBits;
    }

    public AsyncIntegerProperty stopBitsProperty() {
        return this.stopBits;
    }

    public AsyncObjectProperty<Parity> parityProperty() {
        return this.parity;
    }

    public AsyncDoubleProperty minimumHorizontalDistanceProperty() {
        return this.minimumHorizontalDistance;
    }

    public AsyncDoubleProperty minimumVerticalDistanceProperty() {
        return this.minimumVerticalDistance;
    }

}
