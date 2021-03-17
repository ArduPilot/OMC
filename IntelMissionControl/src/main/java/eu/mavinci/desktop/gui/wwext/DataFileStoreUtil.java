/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class DataFileStoreUtil {
    public static void setCacheLocation(File location) {
        // try {
        setCacheLocation(location.getAbsolutePath());
        // setCacheLocation(new String(location.getAbsolutePath().getBytes(System.getProperty("file.encoding")),
        // "UTF-8"));
        // } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    public static void setCacheLocation(String location) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"  encoding=\"UTF-8\" ?>");

        sb.append("<dataFileStore>\n");

        // sb.append("<readLocations><location property=\"gov.nasa.worldwind.platform.alluser.store\" wwDir=\"");
        sb.append("<readLocations><location wwDir=\"");
        sb.append(location);
        sb.append("\" create=\"true\"/></readLocations>\n");

        // sb.append("<writeLocations><location property=\"gov.nasa.worldwind.platform.alluser.store\" wwDir=\"");
        sb.append("<writeLocations><location wwDir=\"");
        sb.append(location);
        sb.append("\" create=\"true\"/></writeLocations>\n");

        sb.append("</dataFileStore>");
        // System.out.println(sb.toString());
        try {
            File file = File.createTempFile("adsc", ".xml");
            file.deleteOnExit();
            try (PrintStream fw = new PrintStream(file, "UTF-8")) {
                fw.print(sb.toString());
                Configuration.setValue(AVKey.DATA_FILE_STORE_CONFIGURATION_FILE_NAME, file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
