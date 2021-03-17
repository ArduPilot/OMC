/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.uploader;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.gui.widgets.IMProgressMonitor;
import eu.mavinci.desktop.gui.widgets.MProgressMonitor;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFile;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;

public abstract class Uploader {

    public static final String KEY = "eu.mavinci.desktop.helper.FtpUploader";

    public interface IScanDirFilter {
        public boolean shouldEnterDir(String relativeParentPath, String folderName);
    }

    long totalSize = 0;
    long currentDone = 0;
    IMProgressMonitor mon;

    String server;
    String user;
    String pw;
    int port;

    public IScanDirFilter scanDirFilter = null;
    private static final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    boolean instantZipUploads = false;

    TreeMap<String, Long> remoteSizes = new TreeMap<String, Long>();

    public boolean instantZipUploads() {
        return instantZipUploads;
    }

    public void setInstantZipUploads(boolean instantZipUploads) {
        this.instantZipUploads = instantZipUploads;
    }

    public String getServer() {
        return server;
    }

    boolean isCancelt;
    String rootDir;

    String lastRemoteDir;

    public Uploader(String server, int port, String user, String pw, String rootDir) {
        this.server = server;
        this.user = user;
        this.pw = pw;
        this.port = port;
        this.rootDir = rootDir;
    }

    public abstract String getFolderPath();

    protected String getFolderPathWithoutProtocoll() {
        if (rootDir.endsWith("/")) {
            return user + "@" + server + rootDir + lastRemoteDir;
        } else {
            return user + "@" + server + rootDir + "/" + lastRemoteDir;
        }
    }

    abstract void setupNewTry() throws Exception;

    abstract void connect() throws Exception;

    abstract void login() throws Exception;

    abstract void goIntoDir(String remoteDir) throws Exception;

    abstract void getFileList() throws Exception;

    abstract void diconnect(boolean soft) throws Exception;

    abstract void upload(File f, String remoteRelativeFile) throws Exception;

    abstract boolean isConnected() throws Exception;

    abstract void delete(String remoteFile) throws Exception;

    abstract void download(File f, String remoteRelativeFile) throws Exception;

    /**
     * returns -1 if file remote does not exist
     *
     * @param remoteName
     * @return
     * @throws Exception
     */
    long getRemoteFileSize(String remoteName) throws Exception {
        Long size = remoteSizes.get(remoteName);
        if (size == null) {
            return -1;
        } else {
            return size;
        }
    }

    public static int DEFAULT_RETRYS = 3;

    public boolean upload(Vector<File> filesRewrite, Vector<File> files, String remoteDir) {
        return upload(filesRewrite, files, remoteDir, DEFAULT_RETRYS);
    }

    public boolean upload(Vector<File> filesRewrite, Vector<File> files, String remoteDir, int noRetries) {
        Vector<UploadFile> filesNew = new Vector<UploadFile>();
        for (File f : files) {
            filesNew.add(new UploadFile(f, false));
        }

        for (File f : filesRewrite) {
            filesNew.add(new UploadFile(f, true));
        }

        return upload(filesNew, remoteDir, noRetries);
    }

    public boolean upload(Vector<UploadFile> files, String remoteDir) {
        return upload(files, remoteDir, DEFAULT_RETRYS, Debug.WARNING);
    }

    public boolean upload(Vector<UploadFile> files, String remoteDir, int noRetries) {
        return upload(files, remoteDir, DEFAULT_RETRYS, Debug.WARNING);
    }

    /**
     * Had to rewrite the upload() method for the Misison Control project here. Reason: Because of too many references
     * to static variables and methods it couldn't be moved in com.intel package without fully rewriting it, and because
     * of the bundles it had to be adapted.
     *
     * @param files
     * @param remoteDir
     * @return
     */
    public boolean uploadForMissionControl(
            Vector<UploadFile> files, String remoteDir, IMProgressMonitor progressMonitor) {
        return uploadForMissionControl(files, remoteDir, DEFAULT_RETRYS, Debug.WARNING, progressMonitor);
    }

    public static final int RETRY_WAIT_SEC = 20;

    /**
     * this is a blocking operation!
     *
     * @param files this files are only uploaded if they are not a file with the same name and size is online
     * @param remoteDir
     * @param noRetries 0 means no retrie (in total one try)
     * @param levelIfOffline e.g. Debug.FINEST, standard Debug.WARNING
     * @return
     */
    public boolean upload(Vector<UploadFile> files, String remoteDir, int noRetries, Level levelIfOffline) {
        TreeMap<String, Integer> duplicatesMap = new TreeMap<String, Integer>();

        lastRemoteDir = remoteDir;
        totalSize = 0;

        TreeSet<String> remoteDirs = new TreeSet<String>();

        for (UploadFile f : files) {
            if (!f.localFile.exists()) {
                continue;
            }

            totalSize += f.localFile.length();
            remoteDirs.add(f.getRemoteFolder());
        }

        // for (String s : remoteDirs){
        // System.out.println("remoteDir="+s);
        // }

        isCancelt = false;

        for (int r = 0; r <= noRetries; r++) {
            // System.out.println("try no:" + r);

            lastDone = currentDone = 0;
            curSpeed = 0;
            duplicatesMap.clear();
            mon =
                new MProgressMonitor(
                    null,
                    languageHelper.getString(KEY + ".monTitle", (r + 1)),
                    languageHelper.getString(KEY + ".monInit"),
                    0,
                    (int)(totalSize / 1024) + 1);
            if (r > 0) {
                for (int t = 0; t != RETRY_WAIT_SEC; t++) {
                    mon.setNote(languageHelper.getString(KEY + ".retryWait", (RETRY_WAIT_SEC - t)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    if (mon.isCanceled()) {
                        isCancelt = true;
                        mon.close();
                        return false;
                    }
                }
            }

            lastTimestamp = System.currentTimeMillis();

            try {
                remoteSizes.clear();
                setupNewTry();
                mon.setNote(languageHelper.getString(KEY + ".connectServer"));
                connect();
                mon.setNote(languageHelper.getString(KEY + ".login"));
                login();

                // the following line works on icaros but not on geoID
                // if (client.isCompressionSupported()) client.setCompressionEnabled(true);

                mon.setNote(languageHelper.getString(KEY + ".createDir"));
                goIntoDir(remoteDir);
                for (String dir : remoteDirs) {
                    // System.out.println("mkdir:" + dir);
                    mkdirs(dir);
                }

                mon.setNote(languageHelper.getString(KEY + ".reqExistingFiles"));
                getFileList();
                if (mon.isCanceled()) {
                    try {
                        diconnect(false);
                    } catch (Exception e1) {
                    }

                    mon.close();
                    mon = null;
                    return false;
                }

                // System.out.println("online files: " +remoteSizes);

                for (UploadFile fUp : files) {
                    File f = fUp.localFile;
                    boolean rewrite = fUp.foreUpload;
                    if (f.isDirectory()) {
                        Debug.getLog().warning("Can upload only files and no folders:" + f);
                        continue;
                    }

                    if (!f.exists()) {
                        Debug.getLog().warning("File not found!! Skipping:" + f);
                        continue;
                    }

                    if (f.getName().endsWith(".log.lck")) {
                        Debug.getLog()
                            .warning(
                                "File extension .log.lck is forbidden to upload, since those logfiles are typically not accessable!! Skipping:"
                                    + f);
                        continue;
                    }

                    boolean isZipped = false;
                    if (instantZipUploads) {

                        // uploading still growing file could confuse progressbar ;-)
                        mon.setProgressNote(
                            languageHelper.getString(
                                KEY + ".zipping", f.getName(), StringHelper.bytesToIngName(curSpeed, -3, true)),
                            (int)(Math.min(totalSize, currentDone) / 1024));
                        curFile = getFileMaybeZipped(f);
                        if (!FileHelper.equals(curFile, f)) {
                            isZipped = true;
                            currentDone += f.length() - curFile.length(); // benefit in size from zipping
                        }
                    } else {
                        curFile = f;
                    }

                    String remoteFile = fUp.getRemotePath();
                    if (isZipped) {
                        remoteFile += ".zip";
                    }

                    if (duplicatesMap.containsKey(remoteFile)) {
                        int no = duplicatesMap.get(remoteFile);
                        remoteFile = fUp.getRemotePath(no + "_");
                        no++;
                        duplicatesMap.put(remoteFile, no);
                    } else {
                        duplicatesMap.put(remoteFile, 1);
                    }

                    // System.out.println("next file:"+f);
                    if (mon.isCanceled()) {
                        isCancelt = true;
                        diconnect(false);
                        mon.close();
                        mon = null;
                        return false;
                    }
                    // check if allready uploaded
                    boolean needToUpload = true;
                    setProgress(".uploading");
                    long s = getRemoteFileSize(remoteFile);
                    if (s == -2) {
                        continue; // skipping folders
                    }

                    if (s >= 0) {
                        if (rewrite || s != curFile.length()) {
                            // remove broken old upload
                            Debug.getLog()
                                .fine("old upload of wrong size found, restart: " + curFile + " " + remoteFile);
                            try {
                                delete(remoteFile);
                            } catch (Exception e) {
                                Debug.getLog().log(Level.FINE, "could not delete remote file " + remoteFile, e);
                            }
                        } else {
                            // match!!
                            // System.out.println("file "+curFile+ " allready found online!");
                            needToUpload = false;
                        }
                    }

                    if (needToUpload) {
                        Debug.getLog().fine("uploading: " + curFile + " to " + remoteFile);
                        upload(curFile, remoteFile);
                        if (!isConnected()) {
                            throw new IllegalStateException("connection manual closed");
                            // System.out.println("returning upload");
                        }
                    } else {
                        currentDone += curFile.length();
                        // uploading still growing file could confuse progressbar ;-)
                        mon.setProgress((int)(Math.min(totalSize, currentDone) / 1024));
                    }
                }

                diconnect(true);
                mon.close();
                mon = null;
                return true;
            } catch (UnknownHostException e) {
                Debug.getLog().log(levelIfOffline, "Could not upload, because this computer is probably offline.");
                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                mon.close();
                mon = null;
                if (isCancelt) {
                    return false;
                }
            } catch (Exception e) {
                if (!isCancelt) {
                    Debug.getLog().log(levelIfOffline, "problems while FTP uploading. current File: " + curFile, e);
                }

                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                mon.close();
                mon = null;
                if (isCancelt) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Had to rewrite the upload() method for the Misison Control project here. Reason: Because of too many references
     * to static variables and methods it couldn't be moved in com.intel package without fully rewriting it, and because
     * of the bundles it had to be adapted. this is a blocking operation!
     *
     * @param files this files are only uploaded if they are not a file with the same name and size is online
     * @param remoteDir
     * @param noRetries 0 means no retrie (in total one try)
     * @param levelIfOffline e.g. Debug.FINEST, standard Debug.WARNING
     * @return
     */
    public boolean uploadForMissionControl(
            Vector<UploadFile> files,
            String remoteDir,
            int noRetries,
            Level levelIfOffline,
            IMProgressMonitor progressMonitor) {
        TreeMap<String, Integer> duplicatesMap = new TreeMap<String, Integer>();

        lastRemoteDir = remoteDir;
        totalSize = 0;

        TreeSet<String> remoteDirs = new TreeSet<String>();

        for (UploadFile f : files) {
            if (!f.localFile.exists()) {
                continue;
            }

            totalSize += f.localFile.length();
            remoteDirs.add(f.getRemoteFolder());
        }

        isCancelt = false;

        for (int r = 0; r <= noRetries; r++) {
            // System.out.println("try no:" + r);

            lastDone = currentDone = 0;
            curSpeed = 0;
            duplicatesMap.clear();
            mon = progressMonitor;
            //            new MProgressMonitor(null, languageHelper.getString(KEY + ".monTitle", (r + 1)),
            // languageHelper.getString(KEY + ".monInit"), 0,
            //                (int)(totalSize / 1024) + 1);
            if (r > 0) {
                for (int t = 0; t != RETRY_WAIT_SEC; t++) {
                    mon.setNote(languageHelper.getString(KEY + ".retryWait", (RETRY_WAIT_SEC - t)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    if (mon.isCanceled()) {
                        isCancelt = true;
                        mon.close();
                        return false;
                    }
                }
            }

            lastTimestamp = System.currentTimeMillis();

            try {
                remoteSizes.clear();
                setupNewTry();
                mon.setNote(languageHelper.getString(KEY + ".connectServer"));
                connect();
                mon.setNote(languageHelper.getString(KEY + ".login"));
                login();

                // the following line works on icaros but not on geoID
                // if (client.isCompressionSupported()) client.setCompressionEnabled(true);

                mon.setNote(languageHelper.getString(KEY + ".createDir"));
                goIntoDir(remoteDir);
                for (String dir : remoteDirs) {
                    // System.out.println("mkdir:" + dir);
                    mkdirs(dir);
                }

                mon.setNote(languageHelper.getString(KEY + ".reqExistingFiles"));
                getFileList();
                if (mon.isCanceled()) {
                    try {
                        diconnect(false);
                    } catch (Exception e1) {
                    }

                    mon.close();
                    mon = null;
                    return false;
                }

                // System.out.println("online files: " +remoteSizes);

                for (UploadFile fUp : files) {
                    File f = fUp.localFile;
                    boolean rewrite = fUp.foreUpload;
                    if (f.isDirectory()) {
                        Debug.getLog().warning("Can upload only files and no folders:" + f);
                        continue;
                    }

                    if (!f.exists()) {
                        Debug.getLog().warning("File not found!! Skipping:" + f);
                        continue;
                    }

                    if (f.getName().endsWith(".log.lck")) {
                        Debug.getLog()
                            .warning(
                                "File extension .log.lck is forbidden to upload, since those logfiles are typically not accessable!! Skipping:"
                                    + f);
                        continue;
                    }

                    boolean isZipped = false;
                    if (instantZipUploads) {

                        // uploading still growing file could confuse progressbar ;-)
                        mon.setProgressNote(
                            languageHelper.getString(
                                KEY + ".zipping", f.getName(), StringHelper.bytesToIngName(curSpeed, -3, true)),
                            (int)(Math.min(totalSize, currentDone) / 1024));
                        curFile = getFileMaybeZipped(f);
                        if (!FileHelper.equals(curFile, f)) {
                            isZipped = true;
                            currentDone += f.length() - curFile.length(); // benefit in size from zipping
                        }
                    } else {
                        curFile = f;
                    }

                    String remoteFile = fUp.getRemotePath();
                    if (isZipped) {
                        remoteFile += ".zip";
                    }

                    if (duplicatesMap.containsKey(remoteFile)) {
                        int no = duplicatesMap.get(remoteFile);
                        remoteFile = fUp.getRemotePath(no + "_");
                        no++;
                        duplicatesMap.put(remoteFile, no);
                    } else {
                        duplicatesMap.put(remoteFile, 1);
                    }

                    // System.out.println("next file:"+f);
                    if (mon.isCanceled()) {
                        isCancelt = true;
                        diconnect(false);
                        mon.close();
                        mon = null;
                        return false;
                    }
                    // check if allready uploaded
                    boolean needToUpload = true;
                    setProgress(".uploading");
                    long s = getRemoteFileSize(remoteFile);
                    if (s == -2) {
                        continue; // skipping folders
                    }

                    if (s >= 0) {
                        if (rewrite || s != curFile.length()) {
                            // remove broken old upload
                            Debug.getLog()
                                .fine("old upload of wrong size found, restart: " + curFile + " " + remoteFile);
                            try {
                                delete(remoteFile);
                            } catch (Exception e) {
                                Debug.getLog().log(Level.FINE, "could not delete remote file " + remoteFile, e);
                            }
                        } else {
                            // match!!
                            // System.out.println("file "+curFile+ " allready found online!");
                            needToUpload = false;
                        }
                    }

                    if (needToUpload) {
                        Debug.getLog().fine("uploading: " + curFile + " to " + remoteFile);
                        upload(curFile, remoteFile);
                        if (!isConnected()) {
                            throw new IllegalStateException("connection manual closed");
                            // System.out.println("returning upload");
                        }
                    } else {
                        currentDone += curFile.length();
                        // uploading still growing file could confuse progressbar ;-)
                        mon.setProgress((int)(Math.min(totalSize, currentDone) / 1024));
                    }
                }

                diconnect(true);
                mon.close();
                mon = null;
                return true;
            } catch (UnknownHostException e) {
                Debug.getLog().log(levelIfOffline, "Could not upload, because this computer is probably offline.");
                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                mon.close();
                mon = null;
                if (isCancelt) {
                    return false;
                }
            } catch (Exception e) {
                if (!isCancelt) {
                    Debug.getLog().log(levelIfOffline, "problems while FTP uploading. current File: " + curFile, e);
                }

                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                mon.close();
                mon = null;
                if (isCancelt) {
                    return false;
                }
            }
        }

        return false;
    }

    public Vector<File> lastWrittenFiles = null;

    public boolean download(TreeMap<String, Long> files, String remoteDir, File localDir, int noRetries) {
        IMProgressMonitor mon =
            new MProgressMonitor(
                null,
                languageHelper.getString(KEY + ".download.monTitle", 1),
                languageHelper.getString(KEY + ".download.monInit"),
                0,
                (int)(totalSize / 1024) + 1);
        return downloadIMC(files, remoteDir, localDir, noRetries, mon);
    }

    @SuppressWarnings("unchecked")
    public boolean downloadIMC(
            TreeMap<String, Long> files, String remoteDir, File localDir, int noRetries, IMProgressMonitor monitor) {
        mon = monitor;
        if (files == remoteSizes) {
            files = (TreeMap<String, Long>)files.clone();
        }

        localDir.mkdirs();
        lastRemoteDir = remoteDir;
        totalSize = 0;
        for (Long s : files.values()) {
            // dont count -2 for folders!
            if (s > 0) {
                totalSize += s;
            }
        }

        isCancelt = false;
        lastWrittenFiles = new Vector<File>();
        for (int r = 0; r <= noRetries; r++) {
            lastDone = currentDone = 0;
            curSpeed = 0;
            mon.setMaximum((int)(totalSize / 1024) + 1);
            if (r > 0) {
                for (int t = 0; t != RETRY_WAIT_SEC; t++) {
                    mon.setNote(languageHelper.getString(KEY + ".retryWait", (RETRY_WAIT_SEC - t)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    if (mon.isCanceled()) {
                        isCancelt = true;
                        mon.close();
                        return false;
                    }
                }
            }

            lastTimestamp = System.currentTimeMillis();

            try {
                remoteSizes.clear();
                setupNewTry();
                mon.setNote(languageHelper.getString(KEY + ".connectServer"));
                connect();
                mon.setNote(languageHelper.getString(KEY + ".login"));
                login();

                // the following line works on icaros but not on geoID
                // if (client.isCompressionSupported()) client.setCompressionEnabled(true);

                mon.setNote(languageHelper.getString(KEY + ".createDir"));
                goIntoDir(remoteDir);

                for (Entry<String, Long> entry : files.entrySet()) {
                    curFile =
                        new File(localDir.getAbsolutePath() + File.separator + MFile.adaptToCurSystem(entry.getKey()));
                    lastWrittenFiles.add(curFile);
                    if (entry.getValue() == -2) {
                        curFile.mkdirs();
                        continue;
                    }

                    String remoteFile = remoteDir + entry.getKey();
                    Debug.getLog().fine("try downloading " + curFile + " <- " + remoteFile);

                    boolean needToDownload = true;
                    if (curFile.exists()) {
                        if (curFile.length() == entry.getValue()) {
                            Debug.getLog()
                                .fine(
                                    "old download of correct size found, skip download of"
                                        + curFile
                                        + " <- "
                                        + remoteFile);
                            needToDownload = false;
                        } else {
                            Debug.getLog()
                                .fine("old download of wrong size found, restart: " + curFile + " <- " + remoteFile);
                            curFile.delete();
                        }
                    } else {
                        curFile.getParentFile().mkdirs();
                    }

                    // System.out.println("next file:"+f);
                    if (mon.isCanceled()) {
                        isCancelt = true;
                        diconnect(false);
                        //  mon.close();
                        mon = null;
                        return false;
                    }

                    setProgress(".downloading");
                    if (needToDownload) {
                        download(curFile, entry.getKey());
                        if (!isConnected()) {
                            throw new IllegalStateException("connection manual closed");
                        }
                    } else {
                        currentDone += curFile.length();
                        mon.setProgress((int)(currentDone / 1024));
                    }
                }

                diconnect(true);
                mon = null;
                return true;
            } catch (UnknownHostException e) {
                Debug.getLog().log(Debug.WARNING, "Could not download, because this computer is probably offline.");
                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                if (isCancelt) {
                    mon = null;
                    return false;
                }
            } catch (Exception e) {
                if (!isCancelt) {
                    Debug.getLog().log(Debug.WARNING, "problems while FTP download. current File: " + curFile, e);
                }

                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                if (isCancelt) {
                    mon = null;
                    return false;
                }
            }
        }

        return false;
    }

    public TreeMap<String, Long> scanDir(String remoteDir, int noRetries) {
        MProgressMonitor monitor =
            new MProgressMonitor(
                null,
                languageHelper.getString(KEY + ".scan.monTitle"),
                languageHelper.getString(KEY + ".monInit"),
                0,
                0);
        return scanDirImc(remoteDir, noRetries, monitor);
    }

    public TreeMap<String, Long> scanDirImc(String remoteDir, int noRetries, IMProgressMonitor monitor) {
        Vector<UploadFile> result = new Vector<UploadFile>();

        lastRemoteDir = remoteDir;
        totalSize = 0;

        isCancelt = false;

        for (int r = 0; r <= noRetries; r++) {
            // System.out.println("try no:" + r);

            lastDone = currentDone = 0;
            curSpeed = 0;
            mon = monitor;
            mon.setMaximum((int)(totalSize / 1024) + 1);
            if (r > 0) {
                for (int t = 0; t != RETRY_WAIT_SEC; t++) {
                    mon.setNote(languageHelper.getString(KEY + ".retryWait", (RETRY_WAIT_SEC - t)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    if (mon.isCanceled()) {
                        isCancelt = true;
                        // mon.close();
                        // mon=null;
                        return null;
                    }
                }
            }

            lastTimestamp = System.currentTimeMillis();

            try {
                result.clear();
                remoteSizes.clear();
                setupNewTry();
                mon.setNote(languageHelper.getString(KEY + ".connectServer"));
                connect();
                mon.setNote(languageHelper.getString(KEY + ".login"));
                login();

                mon.setNote(languageHelper.getString(KEY + ".createDir"));
                goIntoDir(remoteDir);

                mon.setNote(languageHelper.getString(KEY + ".reqExistingFiles"));
                getFileList();

                // System.out.println("online files: " +remoteSizes);

                diconnect(true);
                if (mon.isCanceled()) {
                    mon.close();
                    mon = null;
                    return null;
                }

                mon.close();
                mon = null;
                return remoteSizes;
            } catch (UnknownHostException e) {
                Debug.getLog().log(Debug.WARNING, "Could not scan dirs, because this computer is probably offline.");
                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                mon.close();
                mon = null;
                if (isCancelt) {
                    return null;
                }
            } catch (Exception e) {
                if (!isCancelt) {
                    Debug.getLog().log(Debug.WARNING, "problems while FTP scan dirs. current File: " + curFile, e);
                }

                try {
                    diconnect(false);
                } catch (Exception e1) {
                }

                mon.close();
                mon = null;
                if (isCancelt) {
                    return null;
                }
            }
        }

        return null;
    }

    protected abstract void mkdir(String dir) throws Exception;

    protected void mkdirs(String dir) {
        try {
            mkdir(dir);
        } catch (Exception e) {
            String[] s = dir.split(Pattern.quote("/"));
            String total = "";
            for (int i = 0; i != s.length; i++) {
                if (!total.isEmpty()) {
                    total += "/";
                }

                total += s[i];
                try {
                    mkdir(total);
                } catch (Exception e1) {
                }
            }
        }
    }

    protected File getFileMaybeZipped(File orgFile) {
        if (!instantZipUploads) {
            return orgFile;
        }

        if (MFileFilter.pszMatchingExportFilter.accept(orgFile)) {
            return orgFile;
        }

        if (MFileFilter.jpegFilterInclThumps.accept(orgFile)) {
            return orgFile;
        }

        if (MFileFilter.pngFilter.accept(orgFile)) {
            return orgFile;
        }

        if (MFileFilter.rawFilter.accept(orgFile)) {
            return orgFile;
        }

        if (MFileFilter.archivFilter.accept(orgFile)) {
            return orgFile;
        }

        if (orgFile.getName().equalsIgnoreCase(INSTALL_LOG_FILE)) {
            return orgFile;
        }

        try {
            File f = FileHelper.zipSingleFile(orgFile);
            f.deleteOnExit();
            return f;
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "could not compress file " + orgFile, e);
            return orgFile;
        }
    }

    public static final String INSTALL_LOG_FILE = "install.log";

    String lastProgressKey;

    private void setProgress(String key) {
        lastProgressKey = key;
        mon.setProgressNote(
            languageHelper.getString(KEY + key, curFile.getName(), StringHelper.bytesToIngName(curSpeed, -3, true)),
            (int)(Math.min(currentDone, totalSize) / 1024));
        // mon.setNote(languageHelper.getString(KEY+".uploading", curFile.getName()));
    }

    long lastTimestamp = 0;
    long lastDone = 0;
    double curSpeed = 0;
    File curFile = null;

    public boolean progressChanged() {
        long curTime = System.currentTimeMillis();
        if (curTime != lastTimestamp) {
            curSpeed = 1000. * (currentDone - lastDone) / (double)(curTime - lastTimestamp);
            lastTimestamp = curTime;
            lastDone = currentDone;
        }
        // System.out.println("currentDone:"+currentDone);
        if (mon.isCanceled()) {
            isCancelt = true;
            try {
                diconnect(false);
            } catch (Exception e) {
            }

            return false;
        }

        if (lastProgressKey != null) {
            setProgress(lastProgressKey);
        }

        return true;
    }

}
