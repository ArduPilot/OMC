/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.uploader;

import eu.mavinci.core.helper.StringHelper;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.connectors.FTPProxyConnector;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.net.ssl.SSLSocketFactory;

public class UploaderFTP extends Uploader implements FTPDataTransferListener {

    int security;
    FTPClient client;

    public SSLSocketFactory sslSocketFactory = null;

    public UploaderFTP(String server, int port, String user, String pw, String rootDir, int security) {
        super(server, port, user, pw, rootDir);
        this.security = security;
    }

    @Override
    public String getFolderPath() {
        return "ftp://" + getFolderPathWithoutProtocoll();
    }

    @Override
    public void started() {}

    @Override
    public void transferred(int length) {
        currentDone += length;
        progressChanged();
    }

    @Override
    public void completed() {}

    @Override
    public void aborted() {}

    @Override
    public void failed() {}

    @Override
    void setupNewTry() {
        client = new FTPClient();
        if (sslSocketFactory != null) {
            client.setSSLSocketFactory(sslSocketFactory);
        }

        if (System.getProperty("ftp.proxyHost") != null) {
            client.setConnector(
                new FTPProxyConnector(
                    System.getProperty("ftp.proxyHost"), Integer.parseInt(System.getProperty("ftp.proxyPort"))));
        }
    }

    @Override
    void connect() throws Exception {
        client.setSecurity(security);
        client.connect(server, port);
    }

    @Override
    void login() throws Exception {
        client.login(user, pw);
    }

    @Override
    void goIntoDir(String remoteDir) throws Exception {
        client.changeDirectory(rootDir);
        try {
            client.createDirectory(remoteDir);
        } catch (FTPException e) {
            // error on allready existing folder..
            // e.printStackTrace();
        }
        // System.out.println("change into dir: "+remoteDir);
        if (remoteDir != null && !remoteDir.isEmpty()) {
            client.changeDirectory(remoteDir);
        }
    }

    @Override
    void getFileList() throws Exception {
        String dir = client.currentDirectory();
        getFileList(dir, "");
        client.changeDirectory(dir);
    }

    void getFileList(String dir, String relative) throws Exception {
        if (mon.isCanceled()) {
            return;
        }

        mon.setProgressNote(StringHelper.shortening(relative, 45, 10), 0);
        FTPFile[] list = client.list();
        for (int i = 0; i != list.length; i++) {
            FTPFile ftpFile = list[i];
            if (ftpFile.getType() == FTPFile.TYPE_DIRECTORY) {
                String newDir = dir + "/" + ftpFile.getName();
                client.changeDirectory(newDir);
                if (!relative.isEmpty()) {
                    remoteSizes.put(relative, -2L);
                }

                if (scanDirFilter == null || scanDirFilter.shouldEnterDir(relative, ftpFile.getName())) {
                    getFileList(newDir, relative + ftpFile.getName() + "/");
                }
            } else {
                remoteSizes.put(relative + ftpFile.getName(), ftpFile.getSize());
            }
        }
    }

    @Override
    void diconnect(boolean soft) throws Exception {
        if (soft) {
            client.disconnect(true);
        } else {
            client.abortCurrentConnectionAttempt();
            client.disconnect(false);
        }
    }

    @Override
    void upload(File f, String remoteFile) throws Exception {
        try (InputStream inputF = new FileInputStream(f)) {
            client.upload(remoteFile, inputF, 0, 0, this);
        }
    }

    @Override
    void download(File f, String remoteRelativeFile) throws Exception {
        client.download(remoteRelativeFile, f, 0, this);
    }

    @Override
    protected void mkdir(String dir) throws Exception {
        client.createDirectory(dir);
    }

    @Override
    boolean isConnected() throws Exception {
        return client.isConnected();
    }

    @Override
    void delete(String remoteName) throws Exception {
        // System.out.println("delete:" + remoteName);
        client.deleteFile(remoteName);
    }

}
