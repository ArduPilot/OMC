/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import eu.mavinci.desktop.main.debug.Debug;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import org.apache.commons.io.FileUtils;

public class CFileHelper {

    public static void writeStringToFile(String string, File destination) throws IOException {
        writeStream(new ByteArrayInputStream(string.getBytes("UTF-8")), destination);
    }

    public static void writeResourceToFile(String resource, File destination) throws IOException {
        try (InputStream inputResource = ClassLoader.getSystemResourceAsStream(resource)) {
            writeStream(inputResource, destination);
        }
    }

    public static String createHumanReadableSize(File file, boolean addZeros) {
        return createHumanReadableSize(file.length(), addZeros);
    }

    public static String createHumanReadableSize(double size, boolean addZeros) {
        return createHumanReadableSize(size, -3, addZeros);
    }

    public static String createHumanReadableSize(double size, int digits, boolean addZeros) {
        return StringHelper.bytesToIngName(size, digits, addZeros);
    }

    public static void writeStream(InputStream is, File outFile) throws IOException {
        FileOutputStream os = null;
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            os = new FileOutputStream(outFile);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }

            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }

            if (is != null) {
                is.close();
            }
        }
    }

    public static boolean equals(File a, File b) {
        if (a == null || b == null) {
            return false;
        }

        try {
            String as = a.getCanonicalPath();
            String bs = b.getCanonicalPath();
            return as.equals(bs);
        } catch (IOException e) {
            Debug.getLog().log(Debug.WARNING, "could not compare files " + a + " and " + b, e);
            return false;
        }
    }

    public static boolean equalContent(File f1, File f2) throws IOException {
        return FileUtils.contentEquals(f1, f2);
    }

    /**
     * transform an valid url into a filename
     *
     * @param url
     * @return
     */
    public static String urlToFileName(String url) {
        url = url.trim();
        url = url.replace(":", "_");
        url = url.replace("/", "_");
        url = url.replace(".", "-");
        url = url.replace("\\", "_");
        url = url.replace("&", "-");
        url = url.replace("?", "-");
        url = url.replace("=", "-");
        url = url.replaceAll("[^a-zA-Z0-9_-]", Matcher.quoteReplacement(""));
        url = url.substring(0, Math.min(url.length(), 190));
        while (url.endsWith("_") || url.endsWith("-")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    /**
     * Remove all for filenames invalid chars from a string
     *
     * @param name
     * @return
     */
    public static String makeValidFilenameFromString(String name) {
        return name.replaceAll("[# \\/:\"*?<>|]+", Matcher.quoteReplacement(""));
    }

}
