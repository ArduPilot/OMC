/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit.model.discovery;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BroadcastDiscovery {
    public static final String DISCOVER = "MAVLINK_DISCOVER";

    private final AtomicBoolean discovering = new AtomicBoolean(false);
    private final CopyOnWriteArraySet<DiscoveredDevice> devices = new CopyOnWriteArraySet<>();
    private final Object lock = new Object();
    private ExecutorService executorService;
    private DiscoveryCallback<DiscoveredDevice> discoveryCallback;

    private static void log(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BroadcastDiscovery discovery = new BroadcastDiscovery();

//        for (int i = 0; i < 2; i++) {
        Semaphore semaphore = new Semaphore(0);
        final DiscoveryCallback<DiscoveredDevice> callback = new DiscoveryCallback<DiscoveredDevice>() {
            @Override
            public void onStopped(Exception e) {
                log("on stopped " + e);
                semaphore.release();
            }

            @Override
            public void onStarted() {
                log("on started");
                //discovery.stop();
            }

            @Override
            public void onDeviceDiscovered(DiscoveredDevice device) {
                log("discovered device: " + device);
                semaphore.release();
            }
        };

        discovery.start(callback, 30, TimeUnit.SECONDS);

        semaphore.acquireUninterruptibly();
        Thread.sleep(100000);//        }
        log("_FIN_");
    }

    /**
     * Get a list of siteLocalInterfaces (e.g. 192.168.0.1)
     *
     * @return
     * @throws SocketException
     */
    static Set<InetAddress> getSiteLocalAddresses() throws SocketException {
        Set<InetAddress> set = new HashSet<>();

        for (NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InterfaceAddress interfaceAddress : netint.getInterfaceAddresses()) {
                InetAddress addr = interfaceAddress.getAddress();

                if (addr.isSiteLocalAddress()) {
                    set.add(interfaceAddress.getBroadcast());
                }
            }
        }

        return set;
    }

    public List<DiscoveredDevice> getDevices() {
        return List.copyOf(devices);
    }

    private void onDiscoveryPacket(DiscoveryAdvertisement advertisement, InetSocketAddress socketAddress) {
        DiscoveredDevice device = new DiscoveredDevice(socketAddress, advertisement);

        synchronized (lock) {
            if (devices.contains(device)) return;

            devices.add(device);
        }
        log("new devices found: from " + socketAddress + " adv: " + advertisement + " ");
        try {
            discoveryCallback.onDeviceDiscovered(device);
        } catch (Exception ignore) {
        }
    }

    public void start(DiscoveryCallback<DiscoveredDevice> callback) {
        start(callback, 1000, TimeUnit.DAYS);
    }

    public void start(DiscoveryCallback<DiscoveredDevice> callback, long timeout, TimeUnit timeUnit) {
        if (!discovering.compareAndSet(false, true)) {
            log("already started");
            return;
        }
        this.discoveryCallback = callback;
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
        // this is kind of yucky, we use executorService for job control but it works for now...
        executorService.submit(() -> {
            discoveryCallback.onStarted();
            Exception exception = null;
            try {
                doStartDiscovery(timeout, timeUnit);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            } finally {
                discovering.set(false);
                discoveryCallback.onStopped(exception);
            }
        });
    }

    public void stop() {
        if (!discovering.compareAndSet(true, false)) {
            log("not running");
            return;
        }
        log("stopping");
        executorService.shutdownNow();
    }

    private void doStartDiscovery(long timeout, TimeUnit timeUnit) throws IOException {
        Set<InetAddress> siteLocalAddresses = getSiteLocalAddresses();
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(siteLocalAddresses.size() * 2);

            for (InetAddress siteLocalAddress : siteLocalAddresses) {
                doBroadcastDiscovery(service, siteLocalAddress, 9299);
            }

            service.awaitTermination(timeout, timeUnit);
            log("Timed out");
        } catch (InterruptedException e) {
            log("interrupted");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            log("Shutdown now");
            if (service != null) {
                service.shutdownNow();
            }
        }
    }

    public boolean isDiscovering() {
        return discovering.get();
    }


    /*

    AIRCRAFT: listening on known port for discovery request
         Listen UDP 9299
    GCS: sends broadcast packet to known port and awaits discovery offer

         broadcast udp dstport=9299 srcport=<SOMEPORT> / DISCOVER
    AIRCRAFT:
         send reply to GCS
         send udp dstport=<SOMEPORT> srcport=9299 / DISCOVER_OFFER

    GSC:
        now has connection information for Aircraft

     */

    /*

    How this works:

    Listen Thread: listens for discovery responses on random port.
                   There there may be multiple devices responding to discovery

    Broadcast thread: spams broadcast packet to known port


     */
    private void doBroadcastDiscovery(ExecutorService executorService, InetAddress address, int port) throws IOException, InterruptedException {
        final DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);

        // receive thread
        executorService.submit(() -> {
            byte[] buf = new byte[200];
            log("BroadcastDiscover: staring to listen on port " + socket.getLocalPort());

            for (; ; ) {
                DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(receivePacket);
                    log("receive " + receivePacket.getLength() + " bytes from " + receivePacket.getAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String strrec = new String(buf, 0, receivePacket.getLength(), Charsets.UTF_8);
                log("BroadcastDiscovery: port " + socket.getLocalPort() + " received data " + receivePacket.getAddress().toString() + " : " + receivePacket.getPort());
                log("client received: " + strrec);

                DiscoveryAdvertisement advertisement = DiscoveryAdvertisement.parse(strrec);
                onDiscoveryPacket(advertisement, new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort()));
            }
        });

        // send thread
        executorService.submit(() -> {
            log("BroadcastDiscover: starting to discover on" + address + " : " + port);
            byte[] msgDiscover = DISCOVER.getBytes();
            for (; ; ) {
                DatagramPacket sendPacket = new DatagramPacket(msgDiscover, msgDiscover.length, address, port);
                socket.send(sendPacket);
//                log("client sent: " + sendPacket.getSocketAddress() + " msg " + DISCOVER);
                Thread.sleep(500);
            }
        });

    }

    public static class DiscoveredDevice {
        public InetSocketAddress address;
        public DiscoveryAdvertisement advertisement;

        public DiscoveredDevice(InetSocketAddress address, DiscoveryAdvertisement advertisement) {
            this.address = address;
            this.advertisement = advertisement;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiscoveredDevice that = (DiscoveredDevice) o;
            return Objects.equals(address, that.address) &&
                    Objects.equals(advertisement, that.advertisement);
        }

        @Override
        public String toString() {
            return "DiscoveredDevice[address=" + address + " advert=" + advertisement + "]";
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, advertisement);
        }
    }
}
