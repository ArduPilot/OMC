/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.connection.viewmodel.FtpDataTransferHandler;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFileTransfer;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.plane.FTPManager;
import eu.mavinci.plane.IAirplane;
import it.sauronsoftware.ftp4j.FTPFile;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTransferFtpService {

    public static final Logger log = LoggerFactory.getLogger(DataTransferFtpService.class);

    public static final String FTP_ROOT_PATH = "/";
    public static final String FTP_LOG_PATH = "/logs/";
    public static final String PHOTO_LOG_FILE_EXTENSION = "plg";
    public static final String FLIGHT_LOG_FILE_EXTENSION = "log";
    public static final String TPS_INITIAL_SUFFIX = "_initial.tps";
    public static final String TPS_SUFFIX = ".tps";

    public static void downloadGpsRawData(
            IAirplane plane, IDialogService dialogService, IMProgressMonitor progressMonitor) {
        FtpDataTransferHandler handler =
            new FtpDataTransferHandler() {

                private Predicate<FTPFile> fileFilter =
                    (ftpFile) ->
                        ftpFile.getType() == FTPFile.TYPE_FILE
                            && ftpFile.getName().startsWith("basestation_gnss_rawdata");

                @Override
                public String getRootPath() {
                    return FTP_ROOT_PATH;
                }

                @Override
                public Predicate<FTPFile> getFileFilter() {
                    return fileFilter;
                }

                @Override
                public IAirplaneListenerFileTransfer getDownloadResultHandlerFor(final FTPFile rawDataFtpFile) {
                    return new IAirplaneListenerFileTransfer() {

                        @Override
                        public void recv_fileSendingSucceeded(String path) {}

                        @Override
                        public void recv_fileSendingProgress(String path, Integer progress) {}

                        @Override
                        public void recv_fileSendingCancelled(String path) {}

                        @Override
                        public void recv_fileReceivingSucceeded(String path) {
                            progressMonitor.setNote("Succeed: " + path);
                            File file = new File(getPathToFtp(plane), rawDataFtpFile.getName());
                            if (file.getName().endsWith(TPS_INITIAL_SUFFIX)) {
                                String fileName = file.getAbsolutePath();
                                Date date = new Date();
                                SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
                                date.setTime(Calendar.getInstance().getTimeInMillis());
                                fileName =
                                    fileName.substring(0, fileName.length() - TPS_INITIAL_SUFFIX.length())
                                        + "_"
                                        + form.format(date)
                                        + TPS_SUFFIX;
                                File fileNew = new File(fileName);
                                try {
                                    FileHelper.move(file, fileNew);
                                    file = fileNew;
                                } catch (IOException e) {
                                    log.warn(
                                        "Could not rename default RNSS-RAW data file. Attention, files might become overwritten on next download",
                                        e);
                                }
                            }
                        }

                        @Override
                        public void recv_fileReceivingProgress(String path, Integer progress) {
                            progressMonitor.setProgressNote(path, size.addAndGet(progress));
                        }

                        @Override
                        public void recv_fileReceivingCancelled(String path) {
                            progressMonitor.close();
                        }

                        @Override
                        public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {}
                    };
                }

                @Override
                public boolean isBackendOriented() {
                    return true;
                }
            };
        handler.downloadData(plane, dialogService, progressMonitor);
    }

    public static void downloadGpsDebuggingData(
            IAirplane plane, IDialogService dialogService, IMProgressMonitor progressMonitor) {
        final IAirplaneListenerFileTransfer listOpenAfterDownload =
            new IAirplaneListenerFileTransfer() {

                @Override
                public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {}

                @Override
                public void recv_fileSendingProgress(String path, Integer progress) {}

                @Override
                public void recv_fileReceivingProgress(String path, Integer progress) {}

                @Override
                public void recv_fileSendingSucceeded(String path) {}

                @Override
                public void recv_fileReceivingSucceeded(String path) {
                    File f = new File(getPathToFtp(plane), path);
                    log.debug("path:" + f.getPath());
                    try {
                        FileHelper.openFile(f);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void recv_fileSendingCancelled(String path) {}

                @Override
                public void recv_fileReceivingCancelled(String path) {
                    progressMonitor.close();
                }
            };

        progressMonitor.setMaximum(4);

        try {
            final int maxLogSize = 20000;
            final AtomicInteger counter = new AtomicInteger();
            FTPManager ftpManager = plane.getFTPManager();
            ftpManager.downloadImc(
                "/", "topcon-license-conn.log", getPathToFtp(plane), maxLogSize, listOpenAfterDownload, true);
            progressMonitor.setProgress(counter.incrementAndGet());
            if (progressMonitor.isCanceled()) {
                return;
            }

            ftpManager.downloadImc(
                "/", "topcon-license-ap.log", getPathToFtp(plane), maxLogSize, listOpenAfterDownload, false);
            progressMonitor.setProgress(counter.incrementAndGet());
            if (progressMonitor.isCanceled()) {
                return;
            }

            ftpManager.downloadImc(
                "/", "topcon-info-conn.log", getPathToFtp(plane), maxLogSize, listOpenAfterDownload, true);
            progressMonitor.setProgress(counter.incrementAndGet());
            if (progressMonitor.isCanceled()) {
                return;
            }

            ftpManager.downloadImc(
                "/", "topcon-info-ap.log", getPathToFtp(plane), maxLogSize, listOpenAfterDownload, false);
            progressMonitor.setProgress(counter.incrementAndGet());
            if (progressMonitor.isCanceled()) {
                return;
            }
        } catch (Exception e2) {
            log.warn("Could not download OAF log-files from connector", e2);
            progressMonitor.close();
        }
    }

    public static void downloadLog(
            IAirplane plane, IDialogService dialogService, IMProgressMonitor progressMonitor, String... extensions) {
        FtpDataTransferHandler handler =
            new FtpDataTransferHandler() {

                @Override
                public String getRootPath() {
                    return FTP_LOG_PATH;
                }

                @Override
                public Predicate<FTPFile> getFileFilter() {
                    return ftpFile ->
                        ftpFile.getType() == FTPFile.TYPE_FILE
                            && Arrays.asList(extensions).contains(FilenameUtils.getExtension(ftpFile.getName()));
                }

                @Override
                public IAirplaneListenerFileTransfer getDownloadResultHandlerFor(final FTPFile ftpFile) {
                    return new IAirplaneListenerFileTransfer() {

                        @Override
                        public void recv_fileSendingSucceeded(String path) {
                            // nothing to do
                        }

                        @Override
                        public void recv_fileSendingProgress(String path, Integer progress) {
                            // nothing to do
                        }

                        @Override
                        public void recv_fileSendingCancelled(String path) {
                            // nothing to do
                        }

                        @Override
                        public void recv_fileReceivingSucceeded(String path) {
                            progressMonitor.setNote("Succeed: " + path);
                            File file = new File(getPathToFtp(plane), ftpFile.getName());
                            String fileName = file.getAbsolutePath();
                            String extension = FilenameUtils.getExtension(fileName);
                            Date date = new Date();
                            SimpleDateFormat dateFormatter =
                                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
                            date.setTime(Calendar.getInstance().getTimeInMillis());
                            fileName =
                                String.format(
                                    "%s_%s.%s",
                                    FilenameUtils.removeExtension(fileName), dateFormatter.format(date), extension);
                            File fileNew = new File(fileName);
                            try {
                                FileHelper.move(file, fileNew);
                            } catch (IOException e) {
                                log.warn(
                                    "Could not rename log file. Attention, files might become overwritten on next download",
                                    e);
                            }
                        }

                        @Override
                        public void recv_fileReceivingProgress(String path, Integer progress) {
                            progressMonitor.setProgressNote(path, size.addAndGet(progress));
                        }

                        @Override
                        public void recv_fileReceivingCancelled(String path) {
                            progressMonitor.close();
                        }

                        @Override
                        public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {
                            // nothing to do
                        }
                    };
                }

                @Override
                public boolean isBackendOriented() {
                    return true;
                }
            };
        handler.downloadData(plane, dialogService, progressMonitor);
    }

    public static File getPathToFtp(IAirplane airplane) {
        return airplane.getFTPFolder();
    }
}
