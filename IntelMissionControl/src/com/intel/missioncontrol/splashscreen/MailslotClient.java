/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.splashscreen;

import static com.sun.jna.platform.win32.WinNT.FILE_ATTRIBUTE_NORMAL;
import static com.sun.jna.platform.win32.WinNT.FILE_SHARE_READ;
import static com.sun.jna.platform.win32.WinNT.GENERIC_WRITE;
import static com.sun.jna.platform.win32.WinNT.OPEN_EXISTING;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

public class MailslotClient implements AutoCloseable {

    private final String name;
    private HANDLE file;

    public MailslotClient(String mailslotName) {
        name = "\\\\.\\mailslot\\" + mailslotName;
        ensureFile();
    }

    public void SendMessage(String message) {
        if (!ensureFile()) {
            return;
        }

        IntByReference bytesWritten = new IntByReference();
        byte[] bytes = message.getBytes();
        Kernel32.INSTANCE.WriteFile(file, bytes, bytes.length, bytesWritten, null);
    }

    @Override
    public void close() {
        if (file != null) {
            Kernel32.INSTANCE.CloseHandle(file);
        }
    }

    private boolean ensureFile() {
        if (file != null) {
            return true;
        }

        file =
            Kernel32.INSTANCE.CreateFile(
                name, GENERIC_WRITE, FILE_SHARE_READ, null, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, null);

        if (Pointer.nativeValue(file.getPointer()) == -1) {
            file = null;
            return false;
        }

        return true;
    }

}
