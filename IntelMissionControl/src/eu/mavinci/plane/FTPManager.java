/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.WeakRunnable;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.MBackgroundTaskManager;
import com.intel.missioncontrol.settings.ExpertSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.AirplaneListenerFileTransferDelegator;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFileTransfer;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhotoLogName;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.tcp.TCPConnection;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.ITaggingAlgorithm;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.TaggingAlgorithmA;
import eu.mavinci.desktop.gui.widgets.MProgressMonitor;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.UserNotificationHubSwing;
import eu.mavinci.plane.simjava.AirplaneSim;
import eu.mavinci.plane.tcp.AirplaneTCPconnector;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.util.Duration;

public class FTPManager
        implements IAirplaneListenerFileTransfer,
            IAirplaneListenerBackendConnectionLost,
            IAirplaneListenerPhotoLogName {

    final IAirplane plane;
    final MBackgroundTaskManager backgroundTaskManager;
    final IDialogService dialogService;
    final Set<Supplier<IAirplaneListenerFileTransfer>> photoLogDownloadCallbackSuppliers = new LinkedHashSet<>();
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    public FTPManager(IAirplane plane, MBackgroundTaskManager backgroundTaskManager, IDialogService dialogService) {
        this.plane = plane;
        this.backgroundTaskManager = backgroundTaskManager;
        this.dialogService = dialogService;
        plane.addListener(this);
    }

    public String getFTPurl(boolean onlyUpToBackend) {
        try {
            boolean newFTPprotocol =
                plane.getAirplaneCache().getBackendInfo().protocolVersion
                    >= TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION;
            String s =
                (newFTPprotocol && !onlyUpToBackend)
                    ? plane.getAirplaneCache().getPlaneInfo().getFTPurl()
                    : plane.getAirplaneCache().getBackendStateOffline().getFTPurl();
            if (!newFTPprotocol && !onlyUpToBackend) {
                s += plane.getAirplaneCache().getPlaneInfo().serialNumber.replace(":", "_") + "/";
            }

            return s;
        } catch (AirplaneCacheEmptyException e) {
            // e.printStackTrace();
            // System.out.println("connectionStatefrom last Exception"+ plane.getConnectionState() + " isWriteable" +
            // plane.isWriteable());
            return "";
        }
    }

    public FTPClient getFTPclient(boolean onlyUpToBackend, String remotePath)
            throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException,
                    AirplaneCacheEmptyException {
        boolean newFTPprotocol =
            plane.getAirplaneCache().getBackendInfo().protocolVersion
                >= TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION;
        System.out.println("Get newFTPprotocol. DONE!");
        String host =
            (newFTPprotocol && !onlyUpToBackend)
                ? plane.getAirplaneCache().getPlaneInfo().ftpHost
                : plane.getAirplaneCache().getBackendStateOffline().getHost().getHostString();
        if (host == null) {
            throw new AirplaneCacheEmptyException();
        }

        System.out.println("ftpHost: " + plane.getAirplaneCache().getPlaneInfo().ftpHost);

        int port =
            (newFTPprotocol && !onlyUpToBackend)
                ? plane.getAirplaneCache().getPlaneInfo().ftpPort
                : plane.getAirplaneCache().getBackendStateOffline().getBackend().ftpPort;
        if (port < 0) {
            throw new AirplaneCacheEmptyException();
        }

        String path = "/";
        if (!newFTPprotocol && !onlyUpToBackend) {
            path += plane.getAirplaneCache().getPlaneInfo().serialNumber.replace(":", "_") + "/";
        }

        path += remotePath;

        // AirplaneTCPconnector con = (AirplaneTCPconnector) plane.getAirplaneConnector();
        FTPClient ftp = AirplaneTCPconnector.getFTPClient(host, port);
        ftp.changeDirectory(path);
        return ftp;
    }

    public UploadTask upload(
            final File f,
            final String destFolder,
            final IAirplaneListenerFileTransfer callback,
            boolean inclProgressBar) {
        return upload(f, destFolder, callback, false, inclProgressBar);
    }

    public UploadTask upload(
            final File f,
            final String destFolder,
            IAirplaneListenerFileTransfer callback,
            final boolean onlyUpToBackend,
            boolean inclProgressBar) {
        UploadTask up = new UploadTask();
        up.callback = callback;
        backgroundTaskManager.submitTask(
            "Upload to ftp",
            up,
            up.size,
            () -> {
                upload(f, destFolder, onlyUpToBackend, up);
            });
        return up;
    }

    public UploadTask upload(final File f, final String destFolder, final boolean onlyUpToBackend, UploadTask task) {
        Debug.getLog().fine("upload f:" + f + " destFolder:" + destFolder + " onlyUpToBackend:" + onlyUpToBackend);
        final IAirplaneListenerFileTransfer intCallback;
        final String remotePath = destFolder + f.getName();

        final UploadTask ut = new UploadTask();
        ut.size = f.length();
        ut.destFolder = destFolder;
        boolean newProtocol = true;

        try {
            if (plane.getAirplaneCache().getBackendInfo().protocolVersion
                    < TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION) {
                newProtocol = false;
            }
        } catch (AirplaneCacheEmptyException e1) {
            Debug.getLog().log(Level.WARNING, "could not determine FTP protocoll version. assuming 3", e1);
        }

        final boolean onlyFTP;

        if (newProtocol) {
            onlyFTP = true; // use this as fake...
        } else {
            onlyFTP = onlyUpToBackend;
        }

        ut.onlyFromToBackend = onlyUpToBackend;
        ut.onlyFTP = onlyFTP;
        ut.remotePath = remotePath;
        ut.localFile = f;

        ut.trueCallback = ut.callback;
        synchronized (waitingUpload) {
            waitingUpload.put(remotePath, ut);
        }

        if (queTask(ut)) {
            startUploadTask(ut);
        }

        return ut;
    }

    private void startUploadTask(final UploadTask ut) {
        synchronized (ut) {
            if (ut.startedFTP) {
                return;
            }

            ut.startedFTP = true;
        }

        ut.threadRunning = true;
        try {
            if (plane.getAirplaneConnectorClass() == AirplaneSim.class) {
                // no integrated progress bar, since it has an own one
                FileHelper.copyFile(
                    ut.localFile, ((AirplaneSim)plane.getAirplaneConnector()).getFileForFTPpath(ut.remotePath));
            } else {
                FileInputStream is = null;
                OutputStream os = null;

                try {
                    URL url = new URL(getFTPurl(ut.onlyFromToBackend) + ut.remotePath + ";type=i");
                    URLConnection urlc = url.openConnection();
                    os = urlc.getOutputStream();
                    is = new FileInputStream(ut.localFile);

                    if (ut.callback != null) {
                        int step;

                        int packLength = (int)Math.max(ut.localFile.length() / 1000d, 1000);
                        byte[] buffer = new byte[(int)(packLength)];
                        int totalSize = 0;
                        for (int len; (len = is.read(buffer, 0, packLength)) != -1; ) {
                            if (!ut.threadRunning) {
                                return;
                            }

                            if (ut.cancel) {
                                return;
                            }

                            os.write(buffer, 0, len);
                            os.flush();
                            totalSize += len;
                            step = (int)(-1000 + (1000d * totalSize) / ut.size);
                            final int thisStep = step;
                            if (ut.cancel) {
                                return;
                            }
                            // System.out.println("ftp upload step" + step + " size" + totalSize + " of " + ut.size);
                            Dispatcher.postToUI(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ut.cancel) {
                                            return;
                                        }

                                        ut.callback.recv_fileSendingProgress(ut.remotePath, thisStep);
                                    }
                                });
                        }
                    } else {
                        byte[] buffer = new byte[0xFFFF];
                        for (int len; (len = is.read(buffer)) != -1; ) {
                            if (!ut.threadRunning) {
                                return;
                            }

                            if (ut.cancel) {
                                return;
                            }

                            os.write(buffer, 0, len);
                            os.flush();
                        }
                    }

                    os.flush();
                    // System.out.println("upPath " + remotePath);

                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }

                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }

                    if (!ut.threadRunning || ut.cancel) {
                        stopTask(ut);
                    }
                }
            }

            // System.out.println("upPath " + remotePath);

            if (!ut.cancel) {
                if (ut.onlyFTP) {
                    stopTask(ut);
                    DependencyInjector.getInstance()
                        .getInstanceOf(IApplicationContext.class)
                        .addToast(
                            Toast.of(ToastType.INFO)
                                .setText(languageHelper.getString(KEY + ".sendSucc.msg", ut.localFile, ut.remotePath))
                                .create());
                    if (ut.callback != null) {
                        Dispatcher.postToUI(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ut.callback.recv_fileSendingSucceeded(ut.remotePath);
                                }
                            });
                    }
                } else {
                    // System.out.println("plane: send file " + remotePath);
                    Debug.getLog()
                        .config(
                            "File \""
                                + ut.localFile
                                + "\" transmitted successfully via FTP to: \""
                                + ut.remotePath
                                + "\"");
                    plane.sendFile(ut.remotePath);
                }
            } else {
                // cancelt
                stopTask(ut);
            }
        } catch (Exception e) {
            stopTask(ut);
            // e.printStackTrace();
            // Debug.getUserNotifier().handleDebug("problems transmitting file via FTP: " + e.toString());
            Debug.getLog().log(Debug.WARNING, "problems transmitting file via FTP: " + ut.remotePath, e);
            if (ut.callback != null) {
                Dispatcher.postToUI(
                    new Runnable() {
                        @Override
                        public void run() {
                            ut.callback.recv_fileSendingCancelled(ut.remotePath);
                        }
                    });
            }

            // retry question..
            Dispatcher.postToUI(
                new Runnable() {

                    @Override
                    public void run() {
                        // ask if retry is wished
                        boolean ret =
                            dialogService.requestConfirmation(
                                languageHelper.getString(KEY + ".uploadRetry.tit"),
                                UserNotificationHubSwing.wrapText(
                                    languageHelper.getString(KEY + ".uploadRetry.msg", ut.localFile.getName())));
                        if (ret) {
                            upload(
                                ut.localFile, ut.destFolder, ut.trueCallback, ut.onlyFromToBackend, ut.inclProgressBar);
                        }
                    }
                });

            return;
        }
    }

    public void cancelReceiving(DownloadTask dt) {
        if (dt == null) {
            return;
        }
        // System.out.println("cancel:" +path);
        synchronized (waitingDownload) {
            if (dt.cancel) {
                return;
            }
            // System.out.println("cancelReceiving" + path + " " + waitingDownload);
            stopTask(dt);
            if (plane.isWriteable()) {
                plane.cancelReceiving(dt.remotePath);
            }

            dt.cancel = true;
            // if (dt.downloadThread != null) {
            dt.threadRunning = false;
            // dt.downloadThread = null;
            // }
            if (dt.callback != null) {
                dt.callback.recv_fileReceivingCancelled(dt.remotePath);
            }
        }
    }

    public void cancelSending(UploadTask ut) {
        if (ut == null) {
            return;
        }

        synchronized (waitingUpload) {
            if (ut.cancel) {
                return;
            }
            // System.out.println("cancelReceiving" + path + " " + waitingUpload);
            stopTask(ut);
            if (plane.isWriteable()) {
                plane.cancelSending(ut.remotePath);
            }

            ut.cancel = true;
            ut.threadRunning = false;
            // ut.uploadThread = null;
            if (ut.callback != null) {
                ut.callback.recv_fileSendingCancelled(ut.remotePath);
            }
        }
    }

    // public void download(final String remotePath, final String remoteFilename, final File destFolder, long
    // size,boolean inclProgressBar){
    // download(remotePath, remoteFilename, destFolder,size,null,inclProgressBar);
    // }

    /**
     * Downloading the file given by remotePath/remoteFilename and storing it locally at destFoler/remoteFilename if
     * destFoöder is a folder or at destFolder if destFolder is a filename local files will be overwritten if nessesary
     * local directorys will be created if nessesary
     *
     * <p>if nessesary create local folders
     *
     * @param remoteFolder
     * @param destFolder
     */
    public DownloadTask download(
            final String remoteFolder,
            final String remoteFilename,
            final File destFolder,
            long size,
            IAirplaneListenerFileTransfer callback) {
        return download(remoteFolder, remoteFilename, destFolder, size, callback, false);
    }

    public DownloadTask download(
            final String remoteFolder,
            final String remoteFilename,
            final File destFolder,
            long size,
            IAirplaneListenerFileTransfer callback,
            final boolean onlyFromBackend) {
        DownloadTask dt = new DownloadTask();
        dt.callback = callback;
        backgroundTaskManager.submitTask(
            "Download from FTP",
            dt,
            size,
            () -> {
                downloadImc(remoteFolder, remoteFilename, destFolder, size, onlyFromBackend, dt);
            });
        return dt;
    }

    public DownloadTask downloadImc(
            final String remoteFolder,
            final String remoteFilename,
            final File destFolder,
            long size,
            final boolean onlyFromBackend,
            Task task) {
        return downloadImc(remoteFolder, remoteFilename, destFolder, size, task.callback, onlyFromBackend);
    }

    public DownloadTask downloadImc(
            final String remoteFolder,
            final String remoteFilename,
            final File destFolder,
            long size,
            IAirplaneListenerFileTransfer callback,
            final boolean onlyFromBackend) {
        final IAirplaneListenerFileTransfer intCallback;
        Debug.getLog()
            .fine(
                "download remoteFolder:"
                    + remoteFolder
                    + " remoteFilename:"
                    + remoteFilename
                    + " destFolder:"
                    + destFolder
                    + " size:"
                    + size
                    + " onlyFromBackend:"
                    + onlyFromBackend);
        String remoteFile = remoteFolder + remoteFilename;

        File destFile;
        destFolder.mkdirs();
        if (destFolder.isDirectory()) {
            destFile = new File(destFolder.getPath(), remoteFilename);
        } else {
            destFile = destFolder;
        }

        if (destFile.exists() && !dialogService.requestFileOverwriteConfirmation(destFile.getName())) {
            return null;
        }

        boolean newProtocol = true;
        try {
            if (plane.getAirplaneCache().getBackendInfo().protocolVersion
                    < TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION) {
                newProtocol = false;
            }
        } catch (AirplaneCacheEmptyException e1) {
            Debug.getLog().log(Level.WARNING, "could not determine FTP protocoll version. assuming 3", e1);
        }

        final boolean onlyFTP;

        if (newProtocol) {
            onlyFTP = true; // use this as fake...
        } else {
            onlyFTP = onlyFromBackend;
        }

        DownloadTask dt = new DownloadTask();
        dt.remoteFolder = remoteFolder;
        dt.remoteFilename = remoteFilename;
        dt.localFile = destFile;
        dt.remotePath = remoteFile;
        dt.size = size;
        dt.onlyFTP = onlyFTP;
        dt.onlyFromToBackend = onlyFromBackend;
        dt.localFolder = destFolder;

        dt.trueCallback = callback;
        dt.callback = callback;
        synchronized (waitingDownload) {
            waitingDownload.put(dt.remotePath, dt);
        }

        if (queTask(dt)) {
            startDownloadTask(dt, dialogService);
        }

        return dt;
    }

    private void startDownloadTask(DownloadTask dt, IDialogService dialogService) {
        // System.out.println("StartDownload: "+dt);
        if (dt.onlyFTP) {
            processFTPDownload(dt, dialogService);
        } else {
            // System.out.println("getFile " + remoteFile);
            plane.getFile(dt.remotePath);
        }
    }

    Deque<Task> taskQue = new LinkedBlockingDeque<Task>();

    protected synchronized boolean queTask(Task task) {
        if (DependencyInjector.getInstance()
                    .getInstanceOf(GeneralSettings.class)
                    .getOperationLevel()
                == OperationLevel.DEBUG) {
            return true; // start all down/uploads parallel
        }

        boolean ret = taskQue.isEmpty();
        taskQue.addLast(task);
        return ret;
    }

    private synchronized void stopTask(Task task) {
        // Debug.printStackTrace("stop:"+task+" |"+task.remotePath+ "|" + " "+task.getClass());
        // System.out.println("taskQue-empty:"+taskQue.isEmpty() + " "+taskQue);
        // System.out.println("waitingUpload-empty:"+waitingUpload.isEmpty() + " "+waitingUpload);
        // System.out.println("waitingDownload-empty:"+waitingDownload.isEmpty() + " "+waitingDownload);
        //
        if (task instanceof UploadTask) {
            synchronized (waitingUpload) {
                waitingUpload.remove(task.remotePath, task);
            }
        } else if (task instanceof DownloadTask) {
            synchronized (waitingDownload) {
                waitingDownload.remove(task.remotePath, task);
            }
        } else {
            throw new RuntimeException("task has to be of some kind");
        }

        // maybe start next task!
        if (taskQue.remove(task) && !taskQue.isEmpty()) {
            Task t = taskQue.getFirst();
            // System.out.println("nextTask;"+task);
            if (t instanceof UploadTask) {
                UploadTask ut = (UploadTask)t;
                startUploadTask(ut);
            } else {
                DownloadTask dt = (DownloadTask)t;
                startDownloadTask(dt, null);
            }
        }
        // System.out.println("AFTER taskQue-empty:"+taskQue.isEmpty() + " "+taskQue);
        // System.out.println("AFTER waitingUpload-empty:"+waitingUpload.isEmpty() + " "+waitingUpload);
        // System.out.println("AFTER waitingDownload-empty:"+waitingDownload.isEmpty() + " "+waitingDownload);
    }

    Map<String, UploadTask> waitingUpload = new TreeMap<String, UploadTask>();
    Map<String, DownloadTask> waitingDownload = new TreeMap<String, DownloadTask>();

    public abstract static class Task {
        public boolean startedFTP = false;
        public File localFile;
        public boolean cancel = false;
        public boolean onlyFTP = false;
        public boolean onlyFromToBackend = false;
        public IAirplaneListenerFileTransfer callback;
        public IAirplaneListenerFileTransfer
            trueCallback; // since normal callback might be wrapped with a progressbar ;-)
        public boolean threadRunning = false;
        public long size;
        String remotePath;
        public boolean inclProgressBar;
    }

    public static class UploadTask extends Task {

        public String destFolder;

        @Override
        public String toString() {
            return "UP path=" + remotePath;
        }
    }

    public static class DownloadTask extends Task {
        String remoteFolder;
        String remoteFilename;
        File localFolder;

        @Override
        public String toString() {
            return "DOWN path=" + remotePath + "|localPath=" + localFile;
        }
    }

    private void processFTPDownload(final DownloadTask dt, IDialogService dialogService) {
        synchronized (dt) {
            if (dt.startedFTP) {
                return;
            }

            dt.startedFTP = true;
        }

        // System.out.println("processing dt" + dt);
        dt.threadRunning = true;

        try {
            if (plane.getAirplaneConnectorClass() == AirplaneSim.class) {
                // no integrated progress bar, since it has an own one
                FileHelper.copyFile(
                    ((AirplaneSim)plane.getAirplaneConnector()).getFileForFTPpath(dt.remotePath), dt.localFile);
            } else {
                InputStream is = null;
                FileOutputStream os = null;
                try {
                    URL url = new URL(getFTPurl(dt.onlyFromToBackend) + dt.remotePath + ";type=i");
                    // System.out.println("downloading from here:" + url);
                    URLConnection urlc = url.openConnection();
                    is = urlc.getInputStream();

                    // System.out.println("download FTP thread. callback="+dt.callback);
                    os = new FileOutputStream(dt.localFile);
                    long totalSize = 0;
                    if (dt.callback != null) {
                        // int step=1000;
                        int packLength = (int)Math.max(dt.size / 1000, 1000);
                        // System.out.println("packLength:" + packLength);
                        // final double progressStepFact = dt.size / packLength / 1000;
                        byte[] buffer = new byte[(int)(packLength)];
                        for (int len; (len = is.read(buffer, 0, packLength)) != -1; ) {
                            if (!dt.threadRunning) {
                                return;
                            }

                            if (dt.cancel) {
                                return;
                            }

                            totalSize += len;
                            os.write(buffer, 0, len);
                            // System.out.println("len:" + len);
                            // Thread.sleep(1);
                            os.flush();
                            // step++;
                            // sine for big packLenght, the ftp is not using the whole buffer on one read attemp,
                            // the internal stepcount is bigger than the theoretically one
                            final int thisStep = 1000 + Math.round((float)totalSize / (float)dt.size * 1000f);
                            // System.out.println( thisStep+ "dt.canc"+ dt.cancel);
                            if (dt.cancel) {
                                return;
                            }

                            Dispatcher.postToUI(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dt.cancel) {
                                            return;
                                        }

                                        dt.callback.recv_fileReceivingProgress(dt.remotePath, thisStep);
                                    }
                                });
                        }
                    } else {
                        byte[] buffer = new byte[0xFFFF];
                        for (int len; (len = is.read(buffer)) != -1; ) {
                            if (!dt.threadRunning) {
                                return;
                            }

                            if (dt.cancel) {
                                return;
                            }

                            totalSize += len;
                            os.write(buffer, 0, len);
                            os.flush();
                        }
                    }

                    os.flush();
                    if (totalSize < dt.size) {
                        throw new Exception("download stopped before full filesize was received");
                    }

                    if (dt.callback != null) {
                        dt.callback.recv_fileReceivingSucceeded(dt.remotePath);
                    }

                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }

                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }

                    if (!dt.threadRunning || dt.cancel) {
                        stopTask(dt);
                    }
                }
            }

        } catch (Exception e) {
            // System.out.println("download failed:"+dt);
            // e.printStackTrace();
            stopTask(dt);

            Debug.getLog().log(Debug.WARNING, "problems receive file via FTP: " + dt.remotePath, e);
            // Debug.getUserNotifier().handleDebug("problems receive file via FTP: " + e.toString());
            if (dt.callback != null) {
                Dispatcher.postToUI(
                    new Runnable() {
                        @Override
                        public void run() {
                            dt.callback.recv_fileReceivingCancelled(dt.remotePath);
                        }
                    });
            }
            // retry question..
            Dispatcher.postToUI(
                new Runnable() {
                    @Override
                    public void run() {
                        // ask if retry is wished
                        if (dialogService != null) {
                            boolean res =
                                dialogService.requestConfirmation(
                                    languageHelper.getString(KEY + ".downloadRetry.tit"),
                                    languageHelper.getString(KEY + ".downloadRetry.msg", dt.remoteFilename));

                            if (res) {
                                dt.localFile.delete();
                                downloadImc(
                                    dt.remoteFolder,
                                    dt.remoteFilename,
                                    dt.localFolder,
                                    dt.size,
                                    dt.trueCallback,
                                    dt.onlyFromToBackend);
                            }
                        }
                    }

                });

            return;
        }

        if (!dt.cancel) {
            // if download was a photolog, display number of contained pictures
            if (wasDownloadFileValid(dt)) {
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(KEY + ".recvSucc.msg", dt.remotePath, dt.localFile))
                            .create());
            } else {
                // rename broken download
                File fileBroken = new File(dt.localFile.getParentFile(), "broken_" + dt.localFile.getName());
                try {
                    if (fileBroken.exists()) {
                        fileBroken.delete();
                    }

                    FileHelper.move(dt.localFile, fileBroken);
                } catch (IOException e) {
                    Debug.getLog().log(Level.WARNING, "could not rename broken download file to " + fileBroken, e);
                }

                Dispatcher.postToUI(
                    new Runnable() {

                        @Override
                        public void run() {
                            // ask if retry is wished
                            boolean ret =
                                dialogService.requestConfirmation(
                                    languageHelper.getString(KEY + ".recvSuccPlgBroken.tit"),
                                    UserNotificationHubSwing.wrapText(
                                        languageHelper.getString(KEY + ".recvSuccPlgBroken.msg", dt.localFile)));
                            if (ret) {
                                // IAirplaneListenerFileTransfer callback = dt.callback;
                                // if (callback instanceof DownloadProgress) {
                                // DownloadProgress dp = (DownloadProgress) callback;
                                // callback = dp.slaveCallback;
                                // }
                                // download(dt.remoteFolder, dt.remoteFilename, dt.localFile.getParentFile(), dt.size,
                                // callback, true);
                                download(
                                    dt.remoteFolder,
                                    dt.remoteFilename,
                                    dt.localFolder,
                                    dt.size,
                                    dt.trueCallback,
                                    dt.onlyFromToBackend);
                            }
                        }

                    });
            }

            if (dt.callback != null) {
                Dispatcher.postToUI(
                    new Runnable() {
                        @Override
                        public void run() {
                            dt.callback.recv_fileReceivingSucceeded(dt.remotePath);
                        }
                    });
            }
        }

        stopTask(dt);
    }

    public boolean wasDownloadFileValid(DownloadTask dt) {
        Debug.getLog().log(Level.FINE, "checking downloaded file: " + dt.remoteFilename);
        if (!dt.onlyFTP) {
            if (MFileFilter.flgFilter.accept(dt.localFile) || MFileFilter.photoLogFilter.accept(dt.localFile)) {
                if (dt.localFile.length() < dt.size) {
                    Debug.getLog()
                        .warning(
                            "Downloaded file "
                                + dt.localFile
                                + " is smaller ("
                                + dt.localFile.length()
                                + "byte) than expected ("
                                + dt.size
                                + "byte).");
                    return false;
                }
            } else {
                if (dt.localFile.length() != dt.size) {
                    Debug.getLog()
                        .warning(
                            "Downloaded file "
                                + dt.localFile
                                + " has other size ("
                                + dt.localFile.length()
                                + "byte) than expected ("
                                + dt.size
                                + "byte).");
                    return false;
                }
            }
        }

        // if download was a photolog, display number of contained pictures
        if (MFileFilter.photoLogFilter.accept(dt.localFile)) {
            ITaggingAlgorithm alg = TaggingAlgorithmA.createNewDefaultTaggingAlgorithm();
            try {
                alg.loadLogfile(dt.localFile, false);
                if (!alg.checkLogfileDownload()) {
                    throw new Exception("Lines seems to be missing or corrupted in photolog: " + dt.remoteFilename);
                }

                final String msg =
                    languageHelper.getString(KEY + ".recvSuccCount.msg", alg.getLogsAll().size(), dt.remoteFilename);
                Debug.getLog()
                    .fine(
                        "downloaded plg file \"" + dt.localFile + "\"contains =" + alg.getLogsAll().size() + " images");
                backgroundTaskManager.hintJobStatus(msg);

            } catch (Exception e) {
                Debug.getLog()
                    .log(Debug.WARNING, "Photologfile seems corrupted, please redownload: " + dt.remoteFilename, e);
                return false;
            }
        }

        return true;
    }

    @Override
    public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {}

    @Override
    public void recv_fileReceivingCancelled(String path) {
        synchronized (waitingDownload) {
            DownloadTask dt = waitingDownload.get(path);
            if (dt != null) {
                stopTask(dt);
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(Toast.of(ToastType.ALERT).setText("fileReceivingCancelled: " + path).create());
                if (dt.callback != null) {
                    dt.callback.recv_fileReceivingCancelled(path);
                }
            }
        }
    }

    @Override
    public void recv_fileReceivingProgress(String path, Integer progress) {
        // System.out.println("recv_fileReceivingProgress" + path + " prog=" + progress);
        synchronized (waitingDownload) {
            DownloadTask dt = waitingDownload.get(path);
            if (dt != null) {
                // Debug.getUserNotifier().handleDebug("recv_fileReceivingProgress: " + path + " progress="+ progress);
                if (dt.callback != null) {
                    dt.callback.recv_fileReceivingProgress(path, progress);
                }
            }
        }
    }

    @Override
    public void recv_fileReceivingSucceeded(String path) {
        // System.out.println("receivSucceed "+ path);
        // System.out.println(waitingDownload);
        synchronized (waitingDownload) {
            DownloadTask dt = waitingDownload.get(path);
            // dont stop here, since it has to continue over FTP to be on the PC
            if (dt != null) {
                processFTPDownload(dt, null);
            }
        }
    }

    @Override
    public void recv_fileSendingCancelled(String path) {
        synchronized (waitingUpload) {
            UploadTask ut = waitingUpload.get(path);
            if (ut != null) {
                stopTask(ut);
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(Toast.of(ToastType.ALERT).setText("fileSendingCancelled: " + path).create());
                if (ut.callback != null) {
                    ut.callback.recv_fileSendingCancelled(path);
                }
            }
        }
    }

    @Override
    public void recv_fileSendingProgress(String path, Integer progress) {
        // System.out.println("recv_fileSendingProgress" + path + " prog=" + progress);
        synchronized (waitingUpload) {
            UploadTask ut = waitingUpload.get(path);
            if (ut != null) {
                // System.out.println("ut found " + ut);
                // Debug.getUserNotifier().handleDebug("recv_fileSendingProgress: " + path + " progress="+ progress);
                if (ut.callback != null) {
                    // System.out.println("ut callback called" + ut.callback);
                    ut.callback.recv_fileSendingProgress(path, progress);
                }
            }
        }
    }

    @Override
    public void recv_fileSendingSucceeded(String path) {
        synchronized (waitingUpload) {
            UploadTask ut = waitingUpload.get(path);
            if (ut != null) {
                stopTask(ut);
                DependencyInjector.getInstance()
                    .getInstanceOf(IApplicationContext.class)
                    .addToast(
                        Toast.of(ToastType.INFO)
                            .setText(languageHelper.getString(KEY + ".sendSucc.msg", ut.localFile, ut.remotePath))
                            .create());

                if (ut.callback != null) {
                    ut.callback.recv_fileSendingSucceeded(path);
                }
            }
        }
    }

    public static final String KEY = "eu.mavinci.plane.FTPManager";

    private class DownloadProgress implements IAirplaneListenerFileTransfer, Runnable {

        MProgressMonitor progressMonitor;
        IAirplaneListenerFileTransfer slaveCallback;
        DownloadTask dt;
        final String strBackendToPC = languageHelper.getString(KEY + ".progress.msg.BackendToPC") + " ";
        final String strAirplaneToPC = languageHelper.getString(KEY + ".progress.msg.AirplaneToPC") + " ";
        final String strAirplaneToBackend = languageHelper.getString(KEY + ".progress.msg.AirplaneToBackend") + " ";

        DownloadProgress(DownloadTask dt, IAirplaneListenerFileTransfer slaveCallback) {
            this.slaveCallback = slaveCallback;
            this.dt = dt;
            progressMonitor =
                new MProgressMonitor(
                    null, dt.remotePath, languageHelper.getString(KEY + ".progress.init"), dt.onlyFTP ? 1000 : 0, 2001);
            Dispatcher.schedule(new WeakRunnable(this), Duration.millis(1000L), Duration.millis(1000L));
        }

        @Override
        public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {
            if (slaveCallback != null) {
                slaveCallback.recv_dirListing(parentPath, files, sizes);
            }
        }

        @Override
        public void recv_fileReceivingCancelled(String path) {
            progressMonitor.close();
            if (slaveCallback != null) {
                slaveCallback.recv_fileReceivingCancelled(path);
            }
        }

        @Override
        public void recv_fileReceivingProgress(String path, Integer progress) {
            float size = (progress > 1000 ? progress - 1000 : progress) / 1000f * dt.size;
            String note =
                languageHelper.getString(
                    FTPManager.KEY + ".progress.note",
                    FileHelper.createHumanReadableSize(size, true),
                    FileHelper.createHumanReadableSize(dt.size, true));

            if (progress <= 1000) {
                note = strAirplaneToBackend + note;
            } else {
                // progress = 1000+ (progress-1000)/100;
                if (!dt.onlyFTP || dt.onlyFromToBackend) {
                    note = strBackendToPC + note;
                } else {
                    note = strAirplaneToPC + note;
                }
            }
            // System.out.println("download Note " + note);
            // note += "\n" + path;
            // set progress, but prevent progress window bugs by files which grow during transfer
            int max = progressMonitor.getMaximum();
            progressMonitor.setProgressNote(note, progress >= max ? max - 1 : progress);

            // System.out.println("prog.step " + progress + " -> size" + size + "
            // isCancelt:"+progressMonitor.isCanceled()) ;
            run();
            if (slaveCallback != null) {
                slaveCallback.recv_fileReceivingProgress(path, progress);
            }
        }

        @Override
        public void recv_fileReceivingSucceeded(String path) {
            progressMonitor.close();
            if (slaveCallback != null) {
                slaveCallback.recv_fileReceivingSucceeded(path);
            }
        }

        @Override
        public void recv_fileSendingCancelled(String path) {
            if (slaveCallback != null) {
                slaveCallback.recv_fileSendingCancelled(path);
            }
        }

        @Override
        public void recv_fileSendingProgress(String path, Integer progress) {
            if (slaveCallback != null) {
                slaveCallback.recv_fileSendingProgress(path, progress);
            }
        }

        @Override
        public void recv_fileSendingSucceeded(String path) {
            if (slaveCallback != null) {
                slaveCallback.recv_fileSendingSucceeded(path);
            }
        }

        @Override
        public void run() {
            // since the cancel button are only sampled on real progress, it typically takes 10-20seconds without this
            // additional timer
            // check to realize the cancelation
            if (progressMonitor.isCanceled()) {
                plane.getFTPManager().cancelReceiving(dt);
            }
        }

    }

    private class UploadProgress implements IAirplaneListenerFileTransfer, Runnable {

        MProgressMonitor progressMonitor;
        IAirplaneListenerFileTransfer slaveCallback;
        UploadTask ut;
        final String strPCToBackend = languageHelper.getString(KEY + ".progress.msg.PCToBackend") + " ";
        final String strPCToAirplane = languageHelper.getString(KEY + ".progress.msg.PCToAirplane") + " ";
        final String strBackendToAirplane = languageHelper.getString(KEY + ".progress.msg.BackendToAirplane") + " ";

        UploadProgress(UploadTask ut, IAirplaneListenerFileTransfer slaveCallback) {
            this.slaveCallback = slaveCallback;
            this.ut = ut;
            progressMonitor =
                new MProgressMonitor(
                    null,
                    ut.remotePath,
                    languageHelper.getString(KEY + ".progress.init"),
                    -1000,
                    ut.onlyFTP ? 1 : 1001);
            Dispatcher.schedule(new WeakRunnable(this), Duration.millis(1000L), Duration.millis(1000L));
        }

        @Override
        public void recv_dirListing(String parentPath, MVector<String> files, MVector<Integer> sizes) {
            if (slaveCallback != null) {
                slaveCallback.recv_dirListing(parentPath, files, sizes);
            }
        }

        @Override
        public void recv_fileReceivingCancelled(String path) {
            if (slaveCallback != null) {
                slaveCallback.recv_fileReceivingCancelled(path);
            }
        }

        @Override
        public void recv_fileReceivingProgress(String path, Integer progress) {
            if (slaveCallback != null) {
                slaveCallback.recv_fileReceivingProgress(path, progress);
            }
        }

        @Override
        public void recv_fileReceivingSucceeded(String path) {
            if (slaveCallback != null) {
                slaveCallback.recv_fileReceivingSucceeded(path);
            }
        }

        @Override
        public void recv_fileSendingCancelled(String path) {
            progressMonitor.close();
            if (slaveCallback != null) {
                slaveCallback.recv_fileSendingCancelled(path);
            }
        }

        @Override
        public void recv_fileSendingProgress(String path, Integer progress) {
            float size = ((float)(progress <= 0 ? progress + 1000 : progress)) / 1000f * ut.size;
            // System.out.println("prog.step " + progress + " -> size" + size) ;
            String note =
                languageHelper.getString(
                    FTPManager.KEY + ".progress.note",
                    FileHelper.createHumanReadableSize(size, true),
                    FileHelper.createHumanReadableSize(ut.size, true));
            // System.out.println(note);
            if (progress <= 0) {
                if (!ut.onlyFTP || ut.onlyFromToBackend) {
                    note = strPCToBackend + note;
                } else {
                    note = strPCToAirplane + note;
                    // progress /=100;
                }
            } else {
                note = strBackendToAirplane + note;
            }
            // note += "\n" + path;
            // set progress, but prevent progress window bugs by files which grow during transfer
            int max = progressMonitor.getMaximum();
            progressMonitor.setProgressNote(note, progress >= max ? max - 1 : progress);

            run();
            if (slaveCallback != null) {
                slaveCallback.recv_fileSendingProgress(path, progress);
            }
        }

        @Override
        public void recv_fileSendingSucceeded(String path) {
            progressMonitor.close();
            if (slaveCallback != null) {
                slaveCallback.recv_fileSendingSucceeded(path);
                // System.out.println("sending succeded:"+path + " slave:"+slaveCallback);
            }
        }

        @Override
        public void run() {
            // since the cancel button are only sampled on real progress, it typically takes 10-20seconds without this
            // additional timer
            // check to realize the cancelation
            if (progressMonitor.isCanceled()) {
                cancelSending(ut);
            }
        }

    }

    @Override
    public void err_backendConnectionLost(ConnectionLostReasons reason) {
        synchronized (this) {
            taskQue.clear();
        }

        synchronized (waitingDownload) {
            TreeSet<DownloadTask> clone = new TreeSet<DownloadTask>();
            clone.addAll(waitingDownload.values());
            for (DownloadTask dt : clone) {
                cancelReceiving(dt);
            }

            waitingDownload.clear();
        }

        synchronized (waitingUpload) {
            TreeSet<UploadTask> clone = new TreeSet<UploadTask>();
            clone.addAll(waitingUpload.values());
            for (UploadTask ut : clone) {
                cancelSending(ut);
            }

            waitingUpload.clear();
        }
    }

    @Override
    public void recv_newPhotoLog(String name, Integer bytes) {
        // System.out.println("new photolog: " + name +" b"+bytes);
        boolean enableAutoPLGdownload =
            DependencyInjector.getInstance()
                .getInstanceOf(ExpertSettings.class)
                .getEnableAutoPLGdownload();
        if (!enableAutoPLGdownload) {
            return;
        }

        if (!plane.isWriteable()) {
            return;
        }

        String file = name;
        String folder = "/";
        int pos = name.lastIndexOf("/");
        if (pos >= 0) {
            pos++;
            folder = name.substring(0, pos);
            file = name.substring(pos);
        }

        AirplaneListenerFileTransferDelegator callback = new AirplaneListenerFileTransferDelegator();
        List<IAirplaneListenerFileTransfer> downloadCallbacks = collectPhotologDownloadCallbacks();
        downloadCallbacks.forEach(callback::addTransferListener);
        download(folder, file, plane.getFTPFolder(), bytes, callback);
    }

    public void addPhotoLogDownloadCallbackSupplier(Supplier<IAirplaneListenerFileTransfer> downloadCallbackSupplier) {
        photoLogDownloadCallbackSuppliers.add(downloadCallbackSupplier);
    }

    private List<IAirplaneListenerFileTransfer> collectPhotologDownloadCallbacks() {
        return photoLogDownloadCallbackSuppliers.stream().map(Supplier::get).collect(Collectors.toList());
    }

}
