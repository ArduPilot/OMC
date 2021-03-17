/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.splashscreen;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinBase.SECURITY_ATTRIBUTES;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("FieldCanBeLocal")
class MailslotServer implements AutoCloseable {

    private final HANDLE slotHandle;
    private final Thread thread;
    private volatile boolean running = true;

    MailslotServer(String mailslotName, Consumer<String> messageHandler, Consumer<Exception> errorHandler) {
        String name = "\\\\.\\mailslot\\" + mailslotName;
        DWORD maxSize = new DWORD(0);
        DWORD readTimeout = new DWORD(0);
        SECURITY_ATTRIBUTES securityAttributes = new SECURITY_ATTRIBUTES();
        securityAttributes.bInheritHandle = false;
        slotHandle = MailslotApi.INSTANCE.CreateMailslot(name, maxSize, readTimeout, securityAttributes);
        if (Pointer.nativeValue(slotHandle.getPointer()) == -1) {
            thread = null;
            errorHandler.accept(new RuntimeException("CreateMailslot -> " + Kernel32.INSTANCE.GetLastError()));
        } else {
            thread =
                new Thread(
                    () -> {
                        String eventName = UUID.randomUUID().toString().replace("-", "");
                        DWORDByReference nextMessageSize = new DWORDByReference();
                        DWORDByReference messageCount = new DWORDByReference();
                        IntByReference bytesRead = new IntByReference();
                        OVERLAPPED overlapped = new OVERLAPPED();
                        HANDLE hEvent = null;

                        try {
                            while (running) {
                                hEvent = Kernel32.INSTANCE.CreateEvent(null, false, false, eventName);
                                if (Pointer.nativeValue(hEvent.getPointer()) == 0) {
                                    errorHandler.accept(
                                        new RuntimeException("CreateEvent -> " + Kernel32.INSTANCE.GetLastError()));
                                    return;
                                }

                                overlapped.Offset = 0;
                                overlapped.OffsetHigh = 0;
                                overlapped.hEvent = hEvent;

                                if (MailslotApi.INSTANCE.GetMailslotInfo(
                                        slotHandle, null, nextMessageSize, messageCount, null)) {
                                    if (nextMessageSize.getValue().intValue() > 0) {
                                        while (messageCount.getValue().intValue() > 0) {
                                            byte[] buffer = new byte[nextMessageSize.getValue().intValue()];
                                            if (!Kernel32.INSTANCE.ReadFile(
                                                    slotHandle, buffer, buffer.length, bytesRead, overlapped)) {
                                                errorHandler.accept(
                                                    new RuntimeException(
                                                        "ReadFile -> " + Kernel32.INSTANCE.GetLastError()));
                                                return;
                                            }

                                            messageHandler.accept(new String(buffer));

                                            if (!MailslotApi.INSTANCE.GetMailslotInfo(
                                                    slotHandle, null, nextMessageSize, messageCount, null)) {
                                                break;
                                            }
                                        }
                                    }
                                }

                                Kernel32.INSTANCE.CloseHandle(hEvent);
                                hEvent = null;

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    return;
                                }
                            }
                        } finally {
                            if (hEvent != null) {
                                Kernel32.INSTANCE.CloseHandle(hEvent);
                            }
                        }
                    });

            thread.start();
        }
    }

    @Override
    public void close() {
        running = false;
        Kernel32.INSTANCE.CloseHandle(slotHandle);
    }

}
