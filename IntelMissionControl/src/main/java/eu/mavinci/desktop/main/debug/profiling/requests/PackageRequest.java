/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.desktop.main.debug.profiling.MRequest;

import java.net.InetSocketAddress;

public class PackageRequest extends MRequest {
    InetSocketAddress sender;
    String msg;

    public PackageRequest(InetSocketAddress sender, String msg) {
        super(20L, 100);
        this.sender = sender;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return super.toString() + "\nPackageRequest{sender=" + sender + ", msg=" + msg + "}\n";
    }

    static long slowest = 0;
    static long noSampled = 0;

    @Override
    public synchronized boolean isSlowestUpToNow(long duration) {
        if (duration > slowest) {
            slowest = duration;
            return true;
        }

        return false;
    }

    public synchronized void sampleThis() {
        noSampled++;
    }

    @Override
    public long getCountUpToNow() {
        return noSampled;
    }

}
