/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.DroneConnectionException;
import com.intel.missioncontrol.drone.IDrone;
import java.lang.ref.WeakReference;

// TODO use
public final class WeakDroneConnectionExceptionListener implements IDroneConnectionExceptionListener {

    private final WeakReference<IDroneConnectionExceptionListener> ref;

    public WeakDroneConnectionExceptionListener(IDroneConnectionExceptionListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }

        this.ref = new WeakReference<>(listener);
    }

    public boolean wasGarbageCollected() {
        return (ref.get() == null);
    }

    @Override
    public void onDroneConnectionException(IDrone sender, DroneConnectionException e) {
        IDroneConnectionExceptionListener listener = ref.get();
        if (listener != null) {
            listener.onDroneConnectionException(sender, e);
        } else {
            sender.removeListener(this);
        }
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return this.ref.get() == o;
    }
}
