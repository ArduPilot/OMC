/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.uploader;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSchException;
import eu.mavinci.core.plane.protocol.Base64;
import eu.mavinci.core.plane.protocol.Base64;

public class UploaderMAVinciSCP extends UploaderSCP {

    public UploaderMAVinciSCP() {
        this("/data/");
    }

    public UploaderMAVinciSCP(String basePath) {
        super("mavinci.de", 22, "TODO user", "TODO PW", basePath);
        setInstantZipUploads(true);
    }

    @Override
    public HostKey getExpectedHostKey() throws JSchException {
        String keyB64 =
            "TODO server key";
        return new HostKey("mavinci.de", HostKey.SSHRSA, Base64.decode(keyB64, Base64.DEFAULT));
    }

    @Override
    public String getFolderPath() {
        return "todo path" + rootDir + lastRemoteDir;
    }
}
