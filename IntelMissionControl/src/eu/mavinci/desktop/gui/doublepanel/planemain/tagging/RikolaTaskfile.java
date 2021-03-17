/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

import java.io.File;
import java.util.Vector;

public class RikolaTaskfile {

    public File taskFile;
    public File bandOrderFile;
    public Vector<Double> exposuretime = new Vector<Double>();
    public Vector<String> bandOrder = new Vector<String>();
}
