/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model.discovery;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PortScanDiscovery {


    public static void main(String[] args) throws InterruptedException {
        List<InetSocketAddress> addresses = generatePortRange(InetAddress.getLoopbackAddress(), 5760, 5770);
        List<InetSocketAddress> open = checkPorts(addresses, 1500);

        System.out.println("Found open "+open.size() + "/"+ addresses.size() + " open ports");
        System.out.println("  open ports: "+open.stream().map(i -> i.toString()).collect(Collectors.joining(", ")));
    }

    public static List<InetSocketAddress> generatePortRange(InetAddress address, int startPort, int endPort) {
        if (startPort > endPort) {
            throw new IllegalArgumentException("startport must be > endport");
        }

        ArrayList<InetSocketAddress> list = new ArrayList<>();
        for (int port = startPort; port <= endPort; port++) {
            list.add(new InetSocketAddress(address, port));
        }
        return list;
    };

    /**
     * returns a list of open ports, will block until all ports are checked or timeout
     */
    public static List<InetSocketAddress> checkPorts(Collection<InetSocketAddress> address, int timeoutMillis) throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(address.size());

        List<Callable<InetSocketAddress>> callables = new ArrayList<>();
        for (InetSocketAddress inetSocketAddress : address) {
            callables.add(() -> checkTcpPortConnect(inetSocketAddress));
        }

        List<InetSocketAddress> openSockets = new ArrayList<>();
        for (Future<InetSocketAddress> sock : service.invokeAll(callables, timeoutMillis, TimeUnit.MILLISECONDS)) {
            try {
                InetSocketAddress s = sock.get();
                if (s != null) {
                    openSockets.add(s);
                }

            } catch (Exception ignore) {}
        }

        return openSockets;
    }

    public static InetSocketAddress checkTcpPortConnect(final InetSocketAddress address) {
        try (Socket socket = new Socket()) {
            socket.connect(address);
            socket.close();
            return address;
        } catch (Exception ex) {
            return null;
        }
    }

    public static Future<Integer> checkMavlinkSocket(final ExecutorService executor, final InetAddress host, final int port, final int timeout) {
        return executor.submit(() -> {
            // stupid proxy

            System.out.println(">>>> host"+ host +":"+port);
            //Proxy proxy = new Proxy(Proxy.Type.DIRECT, null);
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeout);
//                socket.setReceiveBufferSize();

                byte[] buff = new byte[128];
                final int maxbytes = 512;
                int bytes = 0;
                Parser parse = new Parser();
                while (bytes < maxbytes) {
                    int read = socket.getInputStream().read(buff);
                    if (read < 0) {
                        return -1;
                    }
                    System.out.println("read on port "+port);
                    //return port;
                    int bytesToParse = Math.min(read, maxbytes - bytes);
                    bytes += bytesToParse;
                    for (int i = 0; i < bytesToParse; i++) {
                        MAVLinkPacket mavLinkPacket = parse.mavlink_parse_char(buff[i] & 0xFF);
                        if (mavLinkPacket != null) {
                            System.out.println("got mavlinkPacket: "+mavLinkPacket.unpack());
                            // found mavlink
                            return port;
                        }
                    }

                }

                return -1;
            } catch (Exception ex) {
                return -1;
            }
        });
    }

}
