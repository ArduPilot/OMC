/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings.rtk;

import com.intel.missioncontrol.settings.Serializable;
import eu.mavinci.desktop.rs232.Rs232Params;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** @author Vladimir Iordanov */
@Serializable
public class RtkSerial {
    private final StringProperty port = new SimpleStringProperty(Rs232Params.getDefaultPort());
    private final ObjectProperty<Integer> bitRate = new SimpleObjectProperty<>(Rs232Params.DEFAULT_BIT_RATE);
    private final ObjectProperty<Rs232Params.DataBits> dataBits =
        new SimpleObjectProperty<>(Rs232Params.DataBits.getDefault());
    private final ObjectProperty<Rs232Params.StopBit> stopBits =
        new SimpleObjectProperty<>(Rs232Params.StopBit.getDefault());
    private final ObjectProperty<Rs232Params.Parity> parity =
        new SimpleObjectProperty<>(Rs232Params.Parity.getDefault());

    public String getPort() {
        return port.get();
    }

    public StringProperty portProperty() {
        return port;
    }

    public Integer getBitRate() {
        return bitRate.get();
    }

    public ObjectProperty<Integer> bitRateProperty() {
        return bitRate;
    }

    public Rs232Params.DataBits getDataBits() {
        return dataBits.get();
    }

    public ObjectProperty<Rs232Params.DataBits> dataBitsProperty() {
        return dataBits;
    }

    public Rs232Params.StopBit getStopBits() {
        return stopBits.get();
    }

    public ObjectProperty<Rs232Params.StopBit> stopBitsProperty() {
        return stopBits;
    }

    public Rs232Params.Parity getParity() {
        return parity.get();
    }

    public ObjectProperty<Rs232Params.Parity> parityProperty() {
        return parity;
    }

    public Rs232Params toRs232Params() {
        return Rs232Params.of(
            "RtkConnection",
            getPort(),
            getBitRate(),
            getDataBits().getValue(),
            getStopBits().getValue(),
            getParity().getValue());
    }

    public void fromRs232Params(Rs232Params rs232Params) {
        port.set(rs232Params.getPort());
        bitRate.set(rs232Params.getBitRate());
        dataBits.set(Rs232Params.DataBits.valueOf(rs232Params.getDataBits()));
        stopBits.set(Rs232Params.StopBit.valueOf(rs232Params.getStopBits()));
        parity.set(Rs232Params.Parity.valueOf(rs232Params.getParity()));
    }
}
