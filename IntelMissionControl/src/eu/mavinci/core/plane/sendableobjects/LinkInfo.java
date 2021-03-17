/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.sendableobjects;

public class LinkInfo extends MObject {
    /** */
    private static final long serialVersionUID = 1656048818640419902L;

    /** time since last received packet from airplane in deci-seconds */
    public int lastConnection = 0;

    /** upstream number of lo packets acked per second */
    public int upPacketsSent = 0;

    /** upstream number of lo packets lost per second */
    public int upPacketsLost = 0;

    /** downstream number of lo packets acked per second */
    public int downPacketsSent = 0;

    /** downstream number of lo packets lost per second */
    public int downPacketsLost = 0;

    /** Used upstream bandwidth in bytes/sec */
    public int upBytesPerSec = 0;

    /** Used downstream bandwidth in bytes/sec */
    public int downBytesPerSec = 0;

    /** Maximum bandwidth in bytes/sec */
    public int linkBandwidth = 0;
}
