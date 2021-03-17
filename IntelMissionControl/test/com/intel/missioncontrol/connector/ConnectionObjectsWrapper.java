/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.connector;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.ConnectionObjects;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;

public class ConnectionObjectsWrapper extends ConnectionObjects {


    private static final InputStream IN;

    static {
        StringReader reader = new StringReader("");
        IN = new ReaderInputStream(reader, Charset.defaultCharset());
    }

    public ConnectionObjectsWrapper() {
        super(IN, null);
    }
}
