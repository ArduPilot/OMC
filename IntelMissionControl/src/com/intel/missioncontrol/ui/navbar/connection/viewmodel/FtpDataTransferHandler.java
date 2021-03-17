/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection.viewmodel;

import com.intel.missioncontrol.ui.dialogs.IDialogService;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFileTransfer;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.plane.IAirplane;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class FtpDataTransferHandler {

    public static final Logger logger = LoggerFactory.getLogger(FtpDataTransferHandler.class);

    protected AtomicInteger size = new AtomicInteger(0);

    public abstract String getRootPath();

    public abstract Predicate<FTPFile> getFileFilter();

    public abstract IAirplaneListenerFileTransfer getDownloadResultHandlerFor(FTPFile ftpFile);

    public abstract boolean isBackendOriented();

    public void downloadData(IAirplane plane, IDialogService dialogService, IMProgressMonitor progressMonitor) {
        progressMonitor.setNote("Get FTP client");

        FTPClient client;
        try {
            client = plane.getFTPManager().getFTPclient(isBackendOriented(), getRootPath());
        } catch (AirplaneCacheEmptyException e) {
            logger.error(
                "Please retry download files from connector when you are or where connected with this session", e);
            progressMonitor.close();
            return;
        } catch (IOException | FTPIllegalReplyException | FTPException e) {
            logger.error("Could not get ftp client!", e);
            progressMonitor.close();
            return;
        }

        progressMonitor.setNote("Get file list");

        FTPFile[] list;
        try {
            list = client.list();
        } catch (Exception e) {
            logger.error("Could not get list of files from FTP!");
            progressMonitor.close();
            return;
        }

        client.abortCurrentConnectionAttempt();

        if (progressMonitor.isCanceled()) {
            return;
        }

        progressMonitor.setNote("Filter and calculate size");
        final List<FTPFile> rawData = new ArrayList<>();
        int size = 0;
        for (FTPFile ftpFile : list) {
            if (getFileFilter().test(ftpFile)) {
                rawData.add(ftpFile);
                size += ftpFile.getSize();
            }
        }

        if (rawData.isEmpty()) {
            logger.warn("No TPS files found on connector!");
            return;
        }

        progressMonitor.setMaximum(size);
        for (FTPFile rawDataFtpFile : rawData) {
            if (progressMonitor.isCanceled()) {
                return;
            }

            plane.getFTPManager()
                .downloadImc(
                    getRootPath(),
                    rawDataFtpFile.getName(),
                    getPathToFtp(plane),
                    rawDataFtpFile.getSize(),
                    getDownloadResultHandlerFor(rawDataFtpFile),
                    isBackendOriented());
        }
    }

    public static File getPathToFtp(IAirplane airplane) {
        return airplane.getFTPFolder();
    }
}
