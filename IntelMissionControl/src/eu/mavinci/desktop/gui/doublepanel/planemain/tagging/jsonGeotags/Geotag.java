/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging.jsonGeotags;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.mavinci.core.obfuscation.IKeepAll;

public class Geotag implements IKeepAll {
    public String filename;
    public Gtag geotag;
    public int schema;

    static final Gson gson = new GsonBuilder().create();

    public static Geotag fromJson(String strLine) {
        return gson.fromJson(strLine, Geotag.class);
    }
}
