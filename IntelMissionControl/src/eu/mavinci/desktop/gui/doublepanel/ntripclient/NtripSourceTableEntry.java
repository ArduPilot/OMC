/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import java.util.Optional;

public abstract class NtripSourceTableEntry {

    protected final EntryType type;

    protected NtripSourceTableEntry(EntryType type) {
        this.type = type;
    }

    public static Optional<NtripSourceTableEntry> parse(String line) {
        String[] parts = line.split(";");
        switch (parts[0]) {
        case "STR":
            return Optional.of(new NtripSourceStr(parts, line));
        case "CAS":
            return Optional.of(new NtripSourceCas(parts, line));
        case "NET":
            return Optional.of(new NtripSourceNet(parts, line));
        default:
            return Optional.empty();
        }
    }

    enum EntryType {
        STR,
        CAS,
        NET
    }
}
