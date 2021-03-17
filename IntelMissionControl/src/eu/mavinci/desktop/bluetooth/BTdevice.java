/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.bluetooth;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;

import javax.bluetooth.RemoteDevice;
import java.io.IOException;
import java.util.Vector;

public class BTdevice {

    public Vector<BTService> getServices() {
        return services;
    }

    public void setServices(Vector<BTService> services) {
        this.services = services;
    }

    public String getDeviceId() {
        return deviceId;
    }

    static String getAdress(RemoteDevice other) {
        return other.getBluetoothAddress();
    }

    static String getName(RemoteDevice other) {
        try {
            return other.getFriendlyName(false);
        } catch (IOException e) {
            return other.getBluetoothAddress();
        }
    }

    public BTdevice(RemoteDevice other) {
        this(getAdress(other), getName(other));
        remoteDevice = other;
    }

    public BTdevice(String id, String name) {
        deviceId = id;
        this.name = name;
    }

    public final String deviceId;
    public final String name;
    public RemoteDevice remoteDevice;

    public static final String KEY = "eu.mavinci.desktop.bluetooth.BluetoothConnectWidget";
    public static final BTdevice DUMMY_DEVICE_REFRESHING =
        new BTdevice(
            "", DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class).getString(KEY + ".refreshing"));

    @Override
    public String toString() {
        if (deviceId.isEmpty()) {
            return name;
        }

        return name + " (" + deviceId + ")";
    }

    public Vector<BTService> services = new Vector<BTService>();

    public boolean hasDefaultDevices;

    void creatDefaultServices() {
        hasDefaultDevices = true;
        services.clear();
        BTService btService =
            new BTService(
                this, "DefaultChannel 1", "btspp://" + deviceId + ":1;authenticate=true;encrypt=true;master=false");
        services.add(btService);

        for (int i = 2; i <= 12; i++) {
            btService =
                new BTService(
                    this,
                    "Channel " + i,
                    "btspp://" + deviceId + ":" + i + ";authenticate=true;encrypt=true;master=false");
            services.add(btService);
        }
    }
}
