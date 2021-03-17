/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.uploader;

import com.jcraft.jsch.JSchException;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.desktop.main.debug.Debug;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.connectors.FTPProxyConnector;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import javax.net.ssl.SSLSocketFactory;

public class UploaderSCPfallbackFTP extends UploaderSCP implements FTPDataTransferListener {

    FTPClient client;

    public SSLSocketFactory sslSocketFactory = null;

    String serverFTP;
    int portFTP;
    String userFTP;
    String pwFTP;
    int securityFTP;
    String rootDirFTP;

    boolean useSCP = true;

    public UploaderSCPfallbackFTP(
            String serverSCP,
            int portSCP,
            String userSCP,
            String pwSCP,
            String rootDirSCP,
            String serverFTP,
            int portFTP,
            String userFTP,
            String pwFTP,
            String rootDirFTP,
            int securityFTP) {
        super(serverSCP, portSCP, userSCP, pwSCP, rootDirSCP);
        this.serverFTP = serverFTP;
        this.portFTP = portFTP;
        this.userFTP = userFTP;
        this.pwFTP = pwFTP;
        this.rootDirFTP = rootDirFTP;
        this.securityFTP = securityFTP;
    }

    @Override
    public String getFolderPath() {
        if (useSCP) {
            return super.getFolderPath();
        } else {
            return "ftp://" + getFolderPathWithoutProtocoll();
        }
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
    void setupNewTry() throws JSchException {
        if (useSCP) {
            super.setupNewTry();
        } else {
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
    }

    @Override
    void connect() throws Exception {
        try {
            useSCP = true;
            throw new Exception();
            // super.connect();
        } catch (Exception e1) {
            useSCP = false;
            try {
                client.setSecurity(securityFTP);
                client.connect(serverFTP, portFTP);
            } catch (Exception e2) {
                Debug.getLog().log(Level.FINE, "even fallback FTP failed!", e2);
                throw e1;
            }
        }
    }

    @Override
    void login() throws Exception {
        if (useSCP) {
            super.login();
        } else {
            client.login(userFTP, pwFTP);
        }
    }

    @Override
    void goIntoDir(String remoteDir) throws Exception {
        if (useSCP) {
            super.goIntoDir(remoteDir);
        } else {
            client.changeDirectory(rootDirFTP);
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
    }

    @Override
    void getFileList() throws Exception {
        if (useSCP) {
            super.getFileList();
        } else {
            String dir = client.currentDirectory();
            getFileList(dir, "");
            client.changeDirectory(dir);
        }
    }

    void getFileList(String dir, String relative) throws Exception {
        if (useSCP) {
            super.getFileList(dir, relative);
        } else {
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
    }

    @Override
    void diconnect(boolean soft) throws Exception {
        if (useSCP) {
            super.diconnect(soft);
        } else {
            if (soft) {
                client.disconnect(true);
            } else {
                client.abortCurrentConnectionAttempt();
                client.disconnect(false);
            }
        }
    }

    @Override
    void upload(File f, String remoteFile) throws Exception {
        if (useSCP) {
            super.upload(f, remoteFile);
        } else {
            try (FileInputStream fis = new FileInputStream(f)) {
                client.upload(remoteFile, fis, 0, 0, this);
            }
        }
    }

    @Override
    void download(File f, String remoteRelativeFile) throws Exception {
        if (useSCP) {
            super.download(f, remoteRelativeFile);
        } else {
            client.download(remoteRelativeFile, f, 0, this);
        }
    }

    @Override
    protected void mkdir(String dir) throws Exception {
        if (useSCP) {
            super.mkdir(dir);
        } else {
            client.createDirectory(dir);
        }
    }

    @Override
    boolean isConnected() throws Exception {
        if (useSCP) {
            return super.isConnected();
        } else {
            return client.isConnected();
        }
    }

    @Override
    void delete(String remoteName) throws Exception {
        if (useSCP) {
            super.delete(remoteName);
        } else {
            // System.out.println("delete:" + remoteName);
            client.deleteFile(remoteName);
        }
    }

}
