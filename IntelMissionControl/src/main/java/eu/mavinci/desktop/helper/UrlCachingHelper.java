/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import eu.mavinci.desktop.main.debug.Debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

public class UrlCachingHelper {
    public static boolean processURLresultsWithCach(File cacheDir, String url,
        IReaderFromCache reader) throws MalformedURLException {
        // ensure legal needs are evaluated when using this method saving cache data, otherwise use processURLresultsWithoutCache
        return processURLresultsWithCach(cacheDir, new URL(url), reader);
    }

    public static boolean processURLresultsWithoutCache(String url, IReaderFromCache reader)
        throws MalformedURLException {
        return processURLresultsWithoutCache(new URL(url), reader);
    }

    public static boolean processURLresultsWithCach(File cacheDir, URL url,
        IReaderFromCache reader) {
        File proxyDataFile =
            new File(cacheDir, FileHelper.urlToFileName(url.toString()) + ".cache");

        File proxyDataFileBackup = new File(proxyDataFile.getAbsolutePath() + "~");
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);
            // int contentLength = httpConn.getContentLength();
            try (InputStream is = httpConn.getInputStream()) {
                FileHelper.writeStream(is, proxyDataFile);
            }

        } catch (Exception e) {
            Debug.getLog().log(Debug.WARNING,
                "Problem accessing URL:" + url + " or writing cache file " + proxyDataFile, e);
            try {
                FileHelper.copyFile(proxyDataFileBackup, proxyDataFile);
            } catch (Exception e1) {
            }
        } finally {
            try {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            } catch (Exception e) {
            }
        }

        if (proxyDataFile.exists()) {
            try (FileInputStream proxyDataFileIS = new FileInputStream(proxyDataFile)) {
                if (reader.readInputFromCache(proxyDataFileIS)) {
                    FileHelper.copyFile(proxyDataFile, proxyDataFileBackup);
                    return true;
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.FINE, "problems reading cachefile " + proxyDataFile, e);
            }
        }

        if (proxyDataFileBackup.exists()) {
            try (FileInputStream proxybackupIS = new FileInputStream(proxyDataFileBackup)) {
                if (reader.readInputFromCache(proxybackupIS)) {
                    FileHelper.copyFile(proxyDataFileBackup, proxyDataFile);
                    return true;
                }
            } catch (Exception e) {
                Debug.getLog()
                    .log(Level.FINE, "problems reading cachefile-backup " + proxyDataFileBackup, e);
            }
        }

        return false;
    }

    public static boolean processURLresultsWithoutCache(URL url, IReaderFromCache reader) {
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(10000);
            try (InputStream is = httpConn.getInputStream()) {
                if (reader.readInputFromCache(is))
                    return true;
            }
        } catch (Exception e) {
            Debug.getLog().log(Debug.WARNING, "Problem accessing URL:" + url, e);
        } finally {
            try {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            } catch (Exception e) {
            }
        }

        return false;
    }

}
