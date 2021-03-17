/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.persistence.local;

import com.intel.missioncontrol.persistence.ResourceResolver;
import com.intel.missioncontrol.project.ResourceReference;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class FileResolver implements ResourceResolver {

    private final Path rootPath;

    public FileResolver(Path rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public Future<InputStream> openInputStreamAsync(ResourceReference reference) {
        try {
            return Futures.successful(new FileInputStream(rootPath.resolve(reference.getName()).toFile()));
        } catch (IOException ex) {
            return Futures.failed(ex);
        }
    }

    @Override
    public Future<OutputStream> openOutputStreamAsync(ResourceReference reference) {
        try {
            return Futures.successful(new FileOutputStream(rootPath.resolve(reference.getName()).toFile()));
        } catch (IOException ex) {
            return Futures.failed(ex);
        }
    }

}
