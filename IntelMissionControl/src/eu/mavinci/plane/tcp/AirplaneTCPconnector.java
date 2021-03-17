/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.tcp;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.tcp.CAirplaneTCPconnector;
import eu.mavinci.core.plane.tcp.TCPConnection;
import eu.mavinci.desktop.main.debug.Debug;
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;
import it.sauronsoftware.ftp4j.connectors.DirectConnector;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.Level;

public class AirplaneTCPconnector extends CAirplaneTCPconnector {

    public static final String KEY = "eu.mavinci.plane.tcp.AirplaneTCPconnector";

    public AirplaneTCPconnector(ICAirplane plane, ConnectionHandler handler, Optional<PortListWaiter> portListWaiter) {
        super(plane, handler, portListWaiter);
    }

    public AirplaneTCPconnector(
            IAirplaneListenerDelegator rootHandler,
            ConnectionHandler handler,
            Optional<PortListWaiter> portListWaiter) {
        super(rootHandler, handler, portListWaiter);
    }

    @Override
    protected void sendStringWithoutBackendPort(String line) {
        if (output != null) {
            super.sendStringWithoutBackendPort(line);
        } else {
            DependencyInjector.getInstance()
                .getInstanceOf(IApplicationContext.class)
                .addToast(
                    Toast.of(ToastType.INFO)
                        .setText(
                            DependencyInjector.getInstance()
                                .getInstanceOf(ILanguageHelper.class)
                                .getString(KEY + ".cantSend"))
                        .create());
        }
    }

    String shouldRequestDirInfo = null;

    @Override
    protected void planeFTPinfoReceived() {
        super.planeFTPinfoReceived();
        if (shouldRequestDirInfo != null) {
            requestDirListing(shouldRequestDirInfo);
        }
    }

    public FTPClient getFTPClient()
            throws AirplaneCacheEmptyException, IllegalStateException, IOException, FTPIllegalReplyException,
                    FTPException {
        if (ftpPortUAV <= 0) {
            throw new AirplaneCacheEmptyException();
        }

        return getFTPClient(ftpHostUAV, ftpPortUAV);
    }

    public static FTPClient getFTPClient(String ftpHost, int ftpPort)
            throws AirplaneCacheEmptyException, IllegalStateException, IOException, FTPIllegalReplyException,
                    FTPException {
        FTPClient client = new FTPClient();
        client.setConnector(
            new DirectConnector() {

                Socket connectingCommunicationChannelSocket;

                @Override
                protected Socket tcpConnectForCommunicationChannel(String host, int port) throws IOException {
                    try {
                        connectingCommunicationChannelSocket = new Socket();
                        // try {
                        //// String hostNet = "10.8.0.";
                        // Debug.getLog().fine("searching for interface to bind FTP to. host:"+getHost());
                        // String hostNet = host.substring(0, getHost().lastIndexOf(".")+1);
                        //// if (hostNet.equals("192.168.1.")) hostNet= "192.168.0."; //FIXME TODO REMOVEME dirty
                        // testing hack
                        // Debug.getLog().fine("searching for interface to bind FTP to. hostNet:"+hostNet);
                        // boolean found = false;
                        // if (hostNet.length() >0 ) {
                        // Enumeration<NetworkInterface> interf = NetworkInterface.getNetworkInterfaces();
                        //// System.out.println("interf:"+interf);
                        // while ( !found && interf.hasMoreElements()){
                        // NetworkInterface nif = interf.nextElement();
                        //// System.out.println("nif:"+nif);
                        // Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
                        //// System.out.println("nifAddresses:"+nifAddresses);
                        // while ( !found && nifAddresses.hasMoreElements()){
                        // InetAddress iad = nifAddresses.nextElement();
                        //// System.out.println("interf. addr:"+iad + "---"+ iad.getHostAddress());
                        // if (iad.getHostAddress().startsWith(hostNet)){
                        // InetSocketAddress bindAdr = new InetSocketAddress(iad, 0);
                        // Debug.getLog().fine("binding socket of FTP connection to:"+bindAdr + " at device:"+nif);
                        // connectingCommunicationChannelSocket.bind(bindAdr);
                        // found = true;
                        // break;
                        // }
                        // }
                        // }
                        // }
                        // if (!found) throw new Exception("no inferface matching to this hostnet found: " +hostNet);
                        // } catch (Exception e){
                        // Debug.getLog().log(Level.WARNING,"binding FTP communication to device failed",e);
                        // }

                        // following code copied from super class
                        connectingCommunicationChannelSocket.setKeepAlive(true);
                        connectingCommunicationChannelSocket.setSoTimeout(readTimeout * 1000);
                        connectingCommunicationChannelSocket.setSoLinger(true, closeTimeout);
                        InetSocketAddress ia = new InetSocketAddress(host, port);
                        connectingCommunicationChannelSocket.connect(ia, connectionTimeout * 1000);
                        return connectingCommunicationChannelSocket;
                    } finally {
                        connectingCommunicationChannelSocket = null;
                    }
                }

                @Override
                public void abortConnectForCommunicationChannel() {
                    if (connectingCommunicationChannelSocket != null) {
                        try {
                            connectingCommunicationChannelSocket.close();
                        } catch (Throwable t) {
                        }
                    }
                }

            });
        client.connect(ftpHost, ftpPort);
        client.login(TCPConnection.DEFAULT_FTP_USER, TCPConnection.DEFAULT_FTP_PW);
        client.SEND_AND_RECEIVE_BUFFER_SIZE = 512;
        return client;
    }

    @Override
    public void requestDirListing(final String path) {
        if (protocolVersion >= TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION) {
            Dispatcher.post(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            FTPClient client = getFTPClient();
                            client.changeDirectory(path);
                            FTPFile[] list = client.list();
                            client.disconnect(false);
                            final MVector<String> files = new MVector<String>(String.class);
                            final MVector<Integer> sizes = new MVector<Integer>(Integer.class);
                            if (!path.isEmpty() && !path.equals("/")) {
                                files.add("..");
                                sizes.add(-1);
                            }

                            for (FTPFile f : list) {
                                files.add(f.getName());
                                sizes.add(f.getType() == FTPFile.TYPE_DIRECTORY ? -1 : (int)f.getSize());
                            }

                            Dispatcher.postToUI(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        rootHandler.recv_dirListing(path, files, sizes);
                                    }
                                });
                            shouldRequestDirInfo = null;
                        } catch (AirplaneCacheEmptyException e) {
                            shouldRequestDirInfo = path;
                            Debug.getLog()
                                .log(
                                    Level.FINE,
                                    "delaying requestDirListing(" + path + ") until backend info is avaliable",
                                    e);
                            Dispatcher.postToUI(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        rootHandler.recv_dirListing(path, null, null);
                                    }
                                });
                        } catch (IllegalStateException
                            | IOException
                            | FTPIllegalReplyException
                            | FTPException
                            | FTPDataTransferException
                            | FTPAbortedException
                            | FTPListParseException e) {
                            // Debug.printStackTrace("path:",path);
                            Debug.getLog()
                                .log(
                                    Debug.WARNING,
                                    "Problems requestDirListing(" + path + ") from " + ftpHostUAV + ":" + ftpPortUAV,
                                    e);
                            Dispatcher.postToUI(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        rootHandler.recv_dirListing(path, null, null);
                                    }
                                });
                        }
                    }
                });
        } else {
            super.requestDirListing(path);
        }
    }

    @Override
    public void makeDir(final String path) {
        if (protocolVersion >= TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION) {
            Dispatcher.post(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            FTPClient client = getFTPClient();
                            client.createDirectory(path);
                            client.disconnect(false);
                        } catch (AirplaneCacheEmptyException e) {
                            Debug.getLog()
                                .log(
                                    Level.WARNING,
                                    "could not perform makeDir(" + path + ") until backend info is avaliable",
                                    e);
                        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException e) {
                            Debug.getLog().log(Debug.WARNING, "Problems makeDir(" + path + ")", e);
                        }
                    }
                });
        } else {
            super.makeDir(path);
        }
    }

    @Override
    public void deleteFile(final String path) {
        if (protocolVersion >= TCPConnection.FTP_TO_AP_STARTING_PROTOCOL_VERSION) {
            Dispatcher.post(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            FTPClient client = getFTPClient();
                            try {
                                client.deleteFile(path);
                            } catch (Exception e) {
                                client.deleteDirectory(path);
                            }

                            client.disconnect(false);
                        } catch (AirplaneCacheEmptyException e) {
                            Debug.getLog()
                                .log(
                                    Level.WARNING,
                                    "could not perform deleteFile(" + path + ") until backend info is avaliable",
                                    e);
                        } catch (IllegalStateException | IOException | FTPIllegalReplyException | FTPException e) {
                            Debug.getLog().log(Debug.WARNING, "Problems deleteFile(" + path + ")", e);
                        }
                    }
                });
        } else {
            super.deleteFile(path);
        }
    }
}
