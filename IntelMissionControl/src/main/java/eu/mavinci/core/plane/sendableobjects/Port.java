/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

public class Port extends MObject {
    private static final long serialVersionUID = -126170246489567495L;
    public String device = "";
    public boolean connected = false;
    public String name = "";
    public PlaneInfoLight info = new PlaneInfoLight();
    public boolean isSimulation = false;

    public Backend backend = null;

    public boolean isCompatible() {
        // return info.isCompatible();
        if (backend != null) {
            return backend.isCompatible();
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Port) {
            Port p = (Port)obj;
            return device.equals(p.device)
                && connected == p.connected
                && name.equals(p.name)
                && info.equals(p.info)
                && isSimulation == p.isSimulation;
        }

        return false;
    }
}
