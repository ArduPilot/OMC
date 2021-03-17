/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;

import java.util.Objects;
import java.util.logging.Level;

public class NtripSourceStr extends NtripSourceTableEntry {

    private final String mountPoint;
    private String identifier;
    private String format;
    private String formatDetails;
    private int carrier;
    private String navSystem;
    private String network;
    private String country;
    private LatLon latLon;
    private boolean needNmea;
    private boolean solutionNetworkBased;
    private String generator;
    private String compressionEncryption;
    private AuthType authType;
    private boolean fee;
    private int bitsPerSeconds;

    public NtripSourceStr(String[] all, String line) {
        super(EntryType.STR);
        mountPoint = all[1];
        try {
            identifier = all[2];
            format = all[3];
            formatDetails = all[4];
            carrier = Integer.parseInt(all[5]);
            navSystem = all[6];
            network = all[7];
            country = all[8];
            latLon = LatLon.fromDegrees(Double.parseDouble(all[9]), Double.parseDouble(all[10]));
            needNmea = "1".equals(all[11]);
            solutionNetworkBased = "1".equals(all[12]);
            generator = all[13];
            compressionEncryption = all[14];
            authType = AuthType.parse(all[15]);
            fee = "Y".equalsIgnoreCase(all[16]);
            bitsPerSeconds = Integer.parseInt(all[17]);
        } catch (Exception e) {
            Debug.getLog().log(Level.FINE, "parsing problems " + line, e);
        }
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFormat() {
        return format;
    }

    public String getFormatDetails() {
        return formatDetails;
    }

    public int getCarrier() {
        return carrier;
    }

    public String getNavSystem() {
        return navSystem;
    }

    public String getNetwork() {
        return network;
    }

    public String getCountry() {
        return country;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public boolean isNeedNmea() {
        return needNmea;
    }

    public boolean isSolutionNetworkBased() {
        return solutionNetworkBased;
    }

    public String getGenerator() {
        return generator;
    }

    public String getCompressionEncryption() {
        return compressionEncryption;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public boolean isFee() {
        return fee;
    }

    public int getBitsPerSeconds() {
        return bitsPerSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NtripSourceStr that = (NtripSourceStr)o;
        return getCarrier() == that.getCarrier()
            && isNeedNmea() == that.isNeedNmea()
            && isSolutionNetworkBased() == that.isSolutionNetworkBased()
            && isFee() == that.isFee()
            && getBitsPerSeconds() == that.getBitsPerSeconds()
            && Objects.equals(getMountPoint(), that.getMountPoint())
            && Objects.equals(getIdentifier(), that.getIdentifier())
            && Objects.equals(getFormat(), that.getFormat())
            && Objects.equals(getFormatDetails(), that.getFormatDetails())
            && Objects.equals(getNavSystem(), that.getNavSystem())
            && Objects.equals(getNetwork(), that.getNetwork())
            && Objects.equals(getCountry(), that.getCountry())
            && Objects.equals(getLatLon(), that.getLatLon())
            && Objects.equals(getGenerator(), that.getGenerator())
            && Objects.equals(getCompressionEncryption(), that.getCompressionEncryption())
            && getAuthType() == that.getAuthType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getMountPoint(),
            getIdentifier(),
            getFormat(),
            getFormatDetails(),
            getCarrier(),
            getNavSystem(),
            getNetwork(),
            getCountry(),
            getLatLon(),
            isNeedNmea(),
            isSolutionNetworkBased(),
            getGenerator(),
            getCompressionEncryption(),
            getAuthType(),
            isFee(),
            getBitsPerSeconds());
    }

    public boolean isCompatible() {
        return true; // TODO
    }

    @Override
    public String toString() {
        return type + "," + getMountPoint() + "," + getFormat();
    }
}
