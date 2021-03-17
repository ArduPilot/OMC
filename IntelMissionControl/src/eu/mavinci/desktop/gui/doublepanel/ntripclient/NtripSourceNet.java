/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.desktop.main.debug.Debug;

import java.util.Objects;
import java.util.logging.Level;

public class NtripSourceNet extends NtripSourceTableEntry {

    private final String identifier;
    private String operator;
    private AuthType authType;
    private boolean fee;
    private String urlNetInfo;
    private String urlStreamInfo;
    private String urlRegistration;

    NtripSourceNet(String[] all, String line) {
        super(EntryType.NET);
        identifier = all[1];
        try {
            operator = all[2];
            authType = AuthType.parse(all[3]);
            fee = "Y".equalsIgnoreCase(all[4]);
            urlNetInfo = all[5];
            urlStreamInfo = all[6];
            urlRegistration = all[7];
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "parsing problems " + line, e);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getOperator() {
        return operator;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public boolean isFee() {
        return fee;
    }

    public String getUrlNetInfo() {
        return urlNetInfo;
    }

    public String getUrlStreamInfo() {
        return urlStreamInfo;
    }

    public String getUrlRegistration() {
        return urlRegistration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NtripSourceNet that = (NtripSourceNet)o;
        return fee == that.fee
            && Objects.equals(identifier, that.identifier)
            && Objects.equals(operator, that.operator)
            && authType == that.authType
            && Objects.equals(urlNetInfo, that.urlNetInfo)
            && Objects.equals(urlStreamInfo, that.urlStreamInfo)
            && Objects.equals(urlRegistration, that.urlRegistration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, operator, authType, fee, urlNetInfo, urlStreamInfo, urlRegistration);
    }

    @Override
    public String toString() {
        return type + "," + identifier;
    }
}
