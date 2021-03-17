/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import com.jogamp.opengl.GLContext;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jogamp.opengl.GLContextImpl;

public class DisplayDeviceInformation {

    private static final List<String> LOWER_PREFERENCES = List.of("HD Graphics", "Intel");
    private static final List<String> HIGHER_PREFERENCES = List.of("nVidia", "AMD");

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class DISPLAY_DEVICE extends Structure implements IKeepAll {
        public WinDef.DWORD cb;
        public char[] DeviceName = new char[32];
        public char[] DeviceString = new char[128];
        public WinDef.DWORD StateFlags;
        public char[] DeviceID = new char[128];
        public char[] DeviceKey = new char[128];

        public DISPLAY_DEVICE() {
            cb = new WinDef.DWORD(size());
        }

        @Override
        protected List<String> getFieldOrder() {
            return List.of("cb", "DeviceName", "DeviceString", "StateFlags", "DeviceID", "DeviceKey");
        }
    }

    private interface User32Ex extends StdCallLibrary, WinUser, WinNT, IKeepAll {
        User32Ex INSTANCE = (User32Ex)Native.loadLibrary("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean EnumDisplayDevicesW(String lpDevice, int iDevNum, DISPLAY_DEVICE lpDisplayDevice, int dwFlags);
    }

    public enum Vendor {
        UNKNOWN,
        NVIDIA,
        AMD
    }

    public static class DeviceInfo {
        private final Vendor vendor;
        private final String name;

        private DeviceInfo(Vendor vendor, String name) {
            this.vendor = vendor;
            this.name = name;
        }

        public Vendor getVendor() {
            return vendor;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class OptimalDeviceInfo {
        private final boolean runningOnOptimalDevice;
        private final DeviceInfo currentDevice;
        private final DeviceInfo optimalDevice;

        private OptimalDeviceInfo(boolean runningOnOptimalDevice, String currentDevice, String optimalDevice) {
            this.runningOnOptimalDevice = runningOnOptimalDevice;

            if (containsIgnoreCase(currentDevice, "nVidia")) {
                this.currentDevice = new DeviceInfo(Vendor.NVIDIA, currentDevice);
            } else if (containsIgnoreCase(currentDevice, "AMD")) {
                this.currentDevice = new DeviceInfo(Vendor.AMD, currentDevice);
            } else {
                this.currentDevice = new DeviceInfo(Vendor.UNKNOWN, currentDevice);
            }

            if (containsIgnoreCase(optimalDevice, "nVidia")) {
                this.optimalDevice = new DeviceInfo(Vendor.NVIDIA, optimalDevice);
            } else if (containsIgnoreCase(optimalDevice, "AMD")) {
                this.optimalDevice = new DeviceInfo(Vendor.AMD, optimalDevice);
            } else {
                this.optimalDevice = new DeviceInfo(Vendor.UNKNOWN, optimalDevice);
            }
        }

        public boolean isRunningOnOptimalDevice() {
            return runningOnOptimalDevice;
        }

        public DeviceInfo getCurrentDevice() {
            return currentDevice;
        }

        public DeviceInfo getOptimalDevice() {
            return optimalDevice;
        }
    }

    public static OptimalDeviceInfo getOptimalDeviceInfo(GLContext context) {
        String renderer;

        try {
            Field vendorField = GLContextImpl.class.getDeclaredField("glRenderer");
            vendorField.setAccessible(true);
            renderer = (String)vendorField.get(context);
        } catch (ReflectiveOperationException ignored) {
            return new OptimalDeviceInfo(true, "", "");
        }

        List<String> availableDevices = enumDisplayDevices();

        for (String lower : LOWER_PREFERENCES) {
            if (containsIgnoreCase(renderer, lower)) {
                for (String higher : HIGHER_PREFERENCES) {
                    for (String device : availableDevices) {
                        if (containsIgnoreCase(device, higher)) {
                            return new OptimalDeviceInfo(false, renderer, device);
                        }
                    }
                }
            }
        }

        return new OptimalDeviceInfo(true, renderer, renderer);
    }

    private static List<String> enumDisplayDevices() {
        List<String> devices = new ArrayList<>();
        DISPLAY_DEVICE device = new DISPLAY_DEVICE();
        for (int i = 0; User32Ex.INSTANCE.EnumDisplayDevicesW(null, i, device, 0); ++i) {
            devices.add(new String(device.DeviceString));
            device = new DISPLAY_DEVICE();
        }

        return devices.stream().distinct().collect(Collectors.toList());
    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }

        int length = searchStr.length();
        if (length == 0) {
            return true;
        }

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length)) {
                return true;
            }
        }

        return false;
    }

}
