/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.asctec.openpayload;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.mavinci.desktop.helper.uploader.Uploader;
import eu.mavinci.desktop.helper.uploader.UploaderFTP;
import it.sauronsoftware.ftp4j.FTPClient;
import eu.mavinci.desktop.helper.uploader.Uploader;
import eu.mavinci.desktop.helper.uploader.UploaderFTP;

import java.io.File;
import java.util.TreeMap;

class OpenPayloadBroadcast {

    public OpenPayloadBroadcast(String json) {
        // JSON parsing of broadcast
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();

        jobject = jobject.getAsJsonObject("general");
        System.out.println(jobject);

        jobject = jobject.getAsJsonObject("lastMission");
        JsonArray jarray = jobject.getAsJsonArray("ftp");
        jobject = jarray.get(0).getAsJsonObject();
        host = jobject.get("address").getAsString();
        int pos = host.indexOf("://");
        if (pos > 0) {
            host = host.substring(pos + 3);
        }

        pos = host.indexOf(":");

        port = 21;
        if (pos > 0) {
            port = Integer.parseInt(host.substring(pos + 1));
            host = host.substring(0, pos);
        }

        user = jobject.get("user").getAsString();
        pass = jobject.get("pass").getAsString();
        dir = "/" + jobject.get("dir").getAsString();
    }

    String host;
    String pass;
    int port;
    String user;
    String dir;

    public String getMissionName() {
        int pos = dir.lastIndexOf("/");
        if (pos < 0) {
            return dir;
        }

        return dir.substring(pos + 1);
    }

    public Uploader getFtp() {
        return new UploaderFTP(host, port, user, pass, dir, FTPClient.SECURITY_FTP);
    }

    public TreeMap<String, Long> downloadFiles(File localFolder) {
        final Uploader up = getFtp();
        TreeMap<String, Long> listing = up.scanDir(dir, 1);
        if (listing != null) {
            up.download(listing, dir, localFolder, Uploader.DEFAULT_RETRYS);
        }

        return listing;
    }

}
