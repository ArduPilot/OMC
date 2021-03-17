/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.licence;

import eu.mavinci.core.helper.MProperties;

import java.util.Set;
import java.util.Vector;

public class AllowedUser extends MProperties {
    private static final long serialVersionUID = 4250372090018254718L;
    Vector<String> macAddresses;
    String displayName;
    String email;
    String name;
    String pcName;

    public AllowedUser() {
        macAddresses = new Vector<String>();
    }

    public String getMacs() {
        String macs = null;
        for (String mac : getMacAddresses()) {
            if (macs == null) {
                macs = mac;
            } else {
                macs += "," + mac;
            }
        }

        return macs;
    }

    public Vector<String> getMacAddresses() {
        return macAddresses;
    }

    public void setMacAddresses(Vector<String> macs) {
        macAddresses = macs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPCname() {
        return pcName;
    }

    public void setPCname(String pcName) {
        this.pcName = pcName;
    }

    public boolean match(Set<String> localMacs) {
        for (String macAllowed : macAddresses) {
            for (String macLocal : localMacs) {
                if (macAllowed.equalsIgnoreCase(macLocal)) {
                    return true;
                }
            }
        }

        return false;
    }

}
