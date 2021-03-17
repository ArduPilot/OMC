/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.uploader;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import eu.mavinci.core.helper.StringHelper;
import java.io.File;
import java.util.Vector;

public class UploaderSCP extends Uploader implements SftpProgressMonitor {

    // long currentDoneFileBegin = 0;

    Session session;

    ChannelSftp channel;

    public UploaderSCP(String server, int port, String user, String pw, String rootDir) {
        super(server, port, user, pw, rootDir);
    }

    public HostKey getExpectedHostKey() throws JSchException {
        return null;
    }

    @Override
    public boolean count(long arg0) {
        currentDone += arg0;
        // System.out.println("count:"+arg0 + " currentDone" + currentDone);
        // currentDone = currentDoneFileBegin + arg0;
        return progressChanged();
    }

    @Override
    public void end() {}

    @Override
    public void init(int arg0, String arg1, String arg2, long arg3) {
        // System.out.println("fileBegin:"+currentDone);
        // currentDoneFileBegin = currentDone;
    }

    JSch jsch;

    @Override
    void setupNewTry() throws JSchException {
        jsch = new JSch();

        HostKey h = getExpectedHostKey();

        if (h != null) {
            HostKeyRepository knownHosts = jsch.getHostKeyRepository();
            knownHosts.add(h, null);
            jsch.setHostKeyRepository(knownHosts);
        }
    }

    @Override
    void connect() throws Exception {
        session = jsch.getSession(user, server, port);
        if (System.getProperty("socksProxyHost") != null) {
            Proxy p =
                new ProxySOCKS5(
                    System.getProperty("socksProxyHost"), Integer.parseInt(System.getProperty("socksProxyPort")));
            session.setProxy(p);
        }

        session.setPassword(pw);
    }

    @Override
    void login() throws Exception {
        session.connect();
        channel = (ChannelSftp)session.openChannel("sftp");
        channel.connect();
    }

    @Override
    void goIntoDir(String remoteDir) throws Exception {
        // String home = channel.getHome();
        // System.out.println("home="+home);
        channel.cd(rootDir);

        try {
            channel.mkdir(remoteDir);
        } catch (Exception e) {
            // error on allready existing folder..
            // e.printStackTrace();
        }

        if (remoteDir != null && !remoteDir.isEmpty()) {
            channel.cd(remoteDir);
            // System.out.println("pwd="+channel.pwd());
        }
    }

    @Override
    void diconnect(boolean soft) throws Exception {
        channel.disconnect();
        session.disconnect();
    }

    @Override
    void upload(File f, String remoteRelativeFile) throws Exception {
        // System.out.println("uploading SCP:"+remoteName);
        channel.put(f.getAbsolutePath(), remoteRelativeFile, this, ChannelSftp.OVERWRITE);
    }

    @Override
    void download(File f, String remoteRelativeFile) throws Exception {
        // System.out.println("pwd="+channel.pwd());
        // System.out.println("getFile:" + remoteRelativeFile + " -> " + f);
        channel.get(remoteRelativeFile, f.getAbsolutePath(), this, ChannelSftp.OVERWRITE);
    }

    @Override
    boolean isConnected() throws Exception {
        return channel.isConnected();
    }

    @Override
    void delete(String remoteName) throws Exception {
        channel.rm(remoteName);
    }

    @Override
    void getFileList() throws Exception {
        String dir = channel.pwd();
        ;
        getFileList(dir, "");
        channel.cd(dir);
    }

    @SuppressWarnings("unchecked")
    void getFileList(String dir, String relative) throws Exception {
        if (mon.isCanceled()) {
            return;
        }

        mon.setNote(StringHelper.shortening(relative, 45, 10));
        Vector<ChannelSftp.LsEntry> list = channel.ls("*");
        // System.out.println("getFileList:"+dir);//+ " -> " +list);
        for (ChannelSftp.LsEntry ftpFile : list) {
            if (ftpFile.getAttrs().isDir()) {
                String newDir = dir + "/" + ftpFile.getFilename();
                channel.cd(newDir);
                if (!relative.isEmpty()) {
                    remoteSizes.put(relative, -2L);
                }

                if (scanDirFilter == null || scanDirFilter.shouldEnterDir(relative, ftpFile.getFilename())) {
                    getFileList(newDir, relative + ftpFile.getFilename() + "/");
                }
            } else {
                remoteSizes.put(relative + ftpFile.getFilename(), ftpFile.getAttrs().getSize());
            }
        }
    }

    @Override
    protected void mkdir(String dir) throws Exception {
        channel.mkdir(dir);
    }

    @Override
    public String getFolderPath() {
        return "sftp://" + getFolderPathWithoutProtocoll();
    }

}
