/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.bluetooth;

import eu.mavinci.desktop.main.debug.Debug;

import javax.bluetooth.ServiceRecord;
import java.util.logging.Level;

@SuppressWarnings("abbreviationaswordinname")
public class BTService {
    BTdevice btDevice;
    String name;
    String url;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        BTService other = (BTService)obj;
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }

        return true;
    }

    public BTService(BTdevice btDevice, String name, String url) {
        this.btDevice = btDevice;
        this.name = name;
        this.url = url;
    }

    ServiceRecord srvRec;

    public BTService(BTdevice btDevice, ServiceRecord srvRec) {
        // System.out.println("BTSErvices serREC");
        this.btDevice = btDevice;
        this.srvRec = srvRec;
        try {
            name = srvRec.getAttributeValue(0x100).getValue().toString();
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "bluetooth service naming fallback", e);
            name = srvRec.toString();
        }
        // name = srvRec .toString().substring(srvRec.toString().indexOf("0x100:")+8+6,
        // srvRec.toString().indexOf("0x1:")-1);
        // +8 to skip line break, tap etc. and + 6 to skip "STRING " / -1 to skip line break at the end

        url = srvRec.getConnectionURL(0, false);
        // System.out.println("name: " + name + " url: " + url);
    }

    public String getUrl() {
        return url;
    }
}
