/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.bluetooth;

import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.settings.ExpertSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.desktop.helper.Recomputer;
import eu.mavinci.desktop.main.debug.Debug;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class BluetoothManager extends Recomputer {

    WeakListenerList<IBluetoothListener> listeners = new WeakListenerList<>("BluetoothManager");

    public static BluetoothManager instance;

    public static void init() {
        // call this after application properties are loaded
        instance = new BluetoothManager();
    }

    public static final Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();
    final Object inquiryCompletedEvent = new Object();

    private BTdevice currentBtDeviceInScan = null;

    DiscoveryListener discoveryListener =
        new DiscoveryListener() {

            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                // System.out.println("Device "+ btDevice.getBluetoothAddress()+ "
                // found");
                devicesDiscovered.addElement(btDevice);
                // System.out.println("devicesDiscovered
                // "+devicesDiscovered.elements());
            }

            public void inquiryCompleted(int discType) {
                // System.out.println("Device Inquiry completed!");
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }

            public void serviceSearchCompleted(int transId, int respCode) {
                // System.out.println("serviceCompleted transID:" + transID + "
                // responseCode:" + respCode);
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }

            public void servicesDiscovered(int transId, ServiceRecord[] servRecord) {
                if (servRecord == null || servRecord.length == 0) {
                    return;
                }

                for (ServiceRecord servRec : servRecord) {
                    if (servRec == null) {
                        continue;
                    }

                    currentBtDeviceInScan.services.add(new BTService(currentBtDeviceInScan, servRec));
                }

                // System.out.println("servicesDiscovered: transID " + transID + " "
                // + Arrays.asList(servRecord) + " adding to " +
                // currentBTDeviceInScan) ;
            }
        };

    List<BTdevice> devices = new LinkedList<BTdevice>();

    public synchronized List<BTdevice> getDevices() {
        return devices;
    }

    public void addListener(IBluetoothListener l) {
        listeners.add(l);
    }

    public void addListenerAtBegin(IBluetoothListener l) {
        listeners.addAtBegin(l);
    }

    public void removeListener(IBluetoothListener l) {
        listeners.remove(l);
    }

    public void refresh() {
        tryStartRecomp();
    }

    // Serial
    // Port
    static final UUID[] uuidSet = new UUID[] {new UUID(0x1101)};
    // UUID[] uuidSet = new UUID[]{ new UUID(0x1105)}; // OBEX Object Push
    // service
    // UUID[] uuidSet = new UUID[]{ new UUID(0x111E)}; // HANDS FREE
    static final int[] attrIDs = new int[] {0x0100}; // Service name

    boolean runningRefresh;

    boolean enableBluetooth;

    public boolean isEnabled() {
        return enableBluetooth;
    }

    private BluetoothManager() {
        // only GPLtainted bluetooth libs are working so far, the D-Bus bindings are not working, if dbus is not
        // working, its silently
        // disabled below
        enableBluetooth =
            DependencyInjector.getInstance()
                .getInstanceOf(ExpertSettings.class)
                .getEnableBluetooth();
        if (!enableBluetooth) {
            // if(GlobalSettings.system == OsTypes.Mac || !enableBLUETOOTH){
            Debug.getLog().info("Bluetooth device discovery disabled");
            return;
        }

        initPersistent(
            new Runnable() {

                @Override
                public void run() {
                    if (!enableBluetooth) {
                        return;
                    }

                    runningRefresh = true;
                    Debug.getLog().info("Bluetooth Start device discovery");
                    Dispatcher.postToUI(
                        new Runnable() {

                            @Override
                            public void run() {
                                for (IBluetoothListener listener : listeners) {
                                    listener.bluetoothDiscoveryStarted();
                                }
                            }

                        });
                    List<BTdevice> devicesNew = new LinkedList<BTdevice>();
                    devicesDiscovered.clear();
                    try {
                        DiscoveryAgent agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
                        synchronized (inquiryCompletedEvent) {
                            boolean started = agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
                            if (!started) {
                                return;
                            }

                            // System.out.println("wait for device inquiry to complete...");
                            inquiryCompletedEvent.wait();
                            // System.out.println(devicesDiscovered.size() + " device(s) found");
                            for (RemoteDevice rd : devicesDiscovered) {
                                devicesNew.add(new BTdevice(rd));
                            }

                            Debug.getLog().info("Bluetooth Start service discovery");
                            // scan for services
                            for (BTdevice btDevice : devicesNew) {
                                currentBtDeviceInScan = btDevice;
                                // System.out.println("Searching for services on " +
                                // btDevice);
                                try {
                                    agent.searchServices(attrIDs, uuidSet, btDevice.remoteDevice, discoveryListener);
                                    // System.out.println("wait start");
                                    // start = new Date().getTime();
                                    inquiryCompletedEvent.wait();
                                    // System.out.println("wait finish");

                                    if (btDevice.services.isEmpty()) {
                                        btDevice.creatDefaultServices();
                                    }

                                } catch (Exception e) {
                                    Debug.getLog()
                                        .log(
                                            Level.WARNING,
                                            "could not discover Bluetooth devices services: " + btDevice,
                                            e);
                                }
                            }
                        }
                    } catch (NoClassDefFoundError e) {
                        Debug.getLog()
                            .log(Level.FINE, "this linux doesent support DBus bluetooth, deactivating bluetooth", e);
                        enableBluetooth = false;
                    } catch (BluetoothStateException e) {
                        if ("Bluetooth Device is not available".equalsIgnoreCase(e.getMessage())
                                || "BluetoothStack not detected".equalsIgnoreCase(e.getMessage())
                                || (e.getMessage() != null && e.getMessage().contains("bluecove_x64.dll"))) {
                            Debug.getLog()
                                .log(
                                    Level.FINE,
                                    "could not discover Bluetooth devices, deactivating bluetooth for this program run",
                                    e);
                            enableBluetooth = false;
                        } else {
                            Debug.getLog().log(Level.WARNING, "could not discover Bluetooth devices", e);
                        }
                    } catch (Exception e) {
                        Debug.getLog().log(Level.WARNING, "could not discover Bluetooth devices", e);
                    } finally {
                        runningRefresh = false;
                        Debug.getLog().info("Bluetooth device discovery ready:" + devicesNew);
                        // if (devicesNew.equals(devices))
                        // return;

                        synchronized (BluetoothManager.this) {
                            devices = devicesNew;
                            Dispatcher.postToUI(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        for (IBluetoothListener listener : listeners) {
                                            listener.bluetoothDiscoveryReady();
                                        }
                                    }

                                });
                        }
                    }
                }
            });

        refresh();
    }

    public boolean isDescovering() {
        return runningRefresh;
    }
}
